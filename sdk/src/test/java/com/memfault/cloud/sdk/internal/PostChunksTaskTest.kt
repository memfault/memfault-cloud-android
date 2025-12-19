package com.memfault.cloud.sdk.internal

import com.memfault.cloud.sdk.ChunkErrorTracker
import com.memfault.cloud.sdk.SendChunksCallback
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val TEST_MAX_CHUNKS_PER_REQUEST = 2

class PostChunksTaskTest {
    private lateinit var task: PostChunksTask
    private lateinit var memfaultHttpApi: MemfaultHttpApi
    private lateinit var chunkQueue: TemporaryChunkQueue
    private lateinit var callback: SendChunksCallback
    private lateinit var errorTracker: ChunkErrorTracker
    private var nowMillis: Long = 0

    @BeforeEach
    fun setUp() {
        memfaultHttpApi = mockk()
        chunkQueue = TemporaryChunkQueue()
        callback = mockk(relaxed = true)
        errorTracker = spyk(ChunkErrorTracker(getElapsedRealtimeMillis = { nowMillis }))
        task =
            PostChunksTask(
                memfaultHttpApi = memfaultHttpApi,
                executor = DirectExecutor(),
                deviceSerial = "DEVICE_SERIAL",
                chunkQueue = chunkQueue,
                callback = callback,
                maxChunksPerRequest = TEST_MAX_CHUNKS_PER_REQUEST,
                errorTracker = errorTracker,
            )
    }

    fun addChunks(count: Int) {
        chunkQueue.addChunks((1..count).map { ByteArray(1) })
    }

    @Test
    fun emptyQueue() {
        task.run()
        verify { callback.onQueueEmpty(0) }
    }

    @Test
    fun notRespectingDelay() {
        addChunks(1)
        errorTracker.trackError()
        val consecutiveErrorCount = errorTracker.consecutiveErrorCount

        task.run()
        verify { errorTracker.nextDelay() }
        verify { callback.onRetryAfterDelay(1, 0, any()) }

        assertEquals(consecutiveErrorCount, errorTracker.consecutiveErrorCount)
    }

    @Test
    fun postSuccess() {
        every {
            memfaultHttpApi.postChunks(any(), any())
        } returns HttpResponse(202, "Accepted", "".byteInputStream(), HttpHeaderMap(emptyMap()), DummyConnection)

        addChunks(1)
        task.run()
        verify { memfaultHttpApi.postChunks(any(), any()) }
        verify { callback.onQueueEmpty(1) }
        verify(exactly = 0) { callback.onRetryAfterDelay(any(), any(), any()) }
        verify { errorTracker.trackSuccess() }

        assertEquals(0, errorTracker.consecutiveErrorCount)
        assertEquals(emptyList<ByteArray>(), chunkQueue.peek(1))
    }

    @Test
    fun postUpToMaxChunks() {
        every {
            memfaultHttpApi.postChunks(any(), any())
        } returns HttpResponse(202, "Accepted", "".byteInputStream(), HttpHeaderMap(emptyMap()), DummyConnection)

        addChunks(TEST_MAX_CHUNKS_PER_REQUEST + 1)
        task.run()
        val chunksSlot = mutableListOf<List<ByteArray>>()
        verify(exactly = 2) { memfaultHttpApi.postChunks(any(), capture(chunksSlot)) }
        verify { callback.onQueueEmpty(TEST_MAX_CHUNKS_PER_REQUEST + 1) }
        verify(exactly = 0) { callback.onRetryAfterDelay(any(), any(), any()) }

        assertEquals(2, chunksSlot.size)
        assertEquals(TEST_MAX_CHUNKS_PER_REQUEST, chunksSlot[0].size)
        assertEquals(1, chunksSlot[1].size)
    }

    @Test
    fun postFailedWithRetryAfterHeader() {
        every {
            memfaultHttpApi.postChunks(any(), any())
        } returns
            HttpResponse(
                429, "Too Many Requests", "".byteInputStream(),
                HttpHeaderMap(mapOf("retry-after" to "123")), DummyConnection,
            )

        addChunks(1)
        task.run()
        verify { memfaultHttpApi.postChunks(any(), any()) }
        verify { callback.onRetryAfterDelay(123, 0, any()) }
        verify(exactly = 0) { callback.onQueueEmpty(any()) }

        assertEquals(123, errorTracker.nextDelay())
    }

    @Test
    fun postFailedNoRetryAfterHeader() {
        every {
            memfaultHttpApi.postChunks(any(), any())
        } returns
            HttpResponse(
                429, "Too Many Requests", "".byteInputStream(), HttpHeaderMap(emptyMap()), DummyConnection,
            )

        addChunks(1)
        task.run()
        verify { memfaultHttpApi.postChunks(any(), any()) }
        verify { callback.onRetryAfterDelay(DEFAULT_RETRY_BASE_DELAY_SECONDS, 0, any()) }
        verify(exactly = 0) { callback.onQueueEmpty(any()) }

        assertEquals(DEFAULT_RETRY_BASE_DELAY_SECONDS, errorTracker.nextDelay())
    }

    private object DummyConnection : CloseableConnection {
        override fun disconnect() = Unit
    }
}
