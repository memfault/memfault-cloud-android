package com.memfault.cloud.sdk.internal

import com.memfault.cloud.sdk.ChunkErrorTracker
import com.memfault.cloud.sdk.ChunkQueue
import com.memfault.cloud.sdk.SendChunksCallback
import java.util.concurrent.Executor

internal class PostChunksTask(
    private val memfaultHttpApi: MemfaultHttpApi,
    private val executor: Executor,
    private val deviceSerial: String,
    private val chunkQueue: ChunkQueue,
    private val callback: SendChunksCallback,
    private val maxChunksPerRequest: Int,
    private val errorTracker: ChunkErrorTracker,
) : Runnable {
    override fun run() {
        var chunksSent = 0
        while (true) {
            val chunksToSend = chunkQueue.peek(maxChunksPerRequest)
            if (chunksToSend.isEmpty()) break
            errorTracker.nextDelay()?.let { delay ->
                // If you are seeing this log message, it means the implementation of onRetryAfterDelay() does not
                // respect the passed delay and causes ChunkSender.send() to get called earlier than the delay.
                // Make sure to *schedule* a re-try, e.g. using https://developer.android.com/reference/android/app/job/JobScheduler
                val exception = Exception("Detected send() before retry delay has passed!")
                Logger.w("Incorrect API usage", exception)
                executor.execute { callback.onRetryAfterDelay(delay, 0, exception) }
                return
            }
            memfaultHttpApi.postChunks(deviceSerial, chunksToSend).use { response ->
                if (response.isSucessful()) {
                    chunksSent += chunksToSend.size
                    chunkQueue.drop(chunksToSend.size)
                    errorTracker.trackSuccess()
                    return@use
                }
                // Note: the retry-after value is used as *base* delay on top of which we back-off exponentially:
                val retryAfter: String? = response?.headers?.get("Retry-After")
                errorTracker.baseDelaySeconds = retryAfter?.toLong() ?: DEFAULT_RETRY_BASE_DELAY_SECONDS
                if (errorTracker.trackError() > MAX_CONSECUTIVE_ERRORS) {
                    // When reaching MAX_CONSECUTIVE_ERRORS, start dropping chunks from the queue, even if they have
                    // not been sent, as a last resort measure, to avoid accumulating too many chunks on the device:
                    chunkQueue.drop(chunksToSend.size)
                }
                executor.execute {
                    callback.onRetryAfterDelay(
                        errorTracker.nextDelay() ?: DEFAULT_RETRY_BASE_DELAY_SECONDS,
                        chunksSent,
                        Exception("Request failed $response"),
                    )
                }
                return
            }
        }
        executor.execute {
            callback.onQueueEmpty(chunksSent)
        }
    }
}

internal const val DEFAULT_RETRY_BASE_DELAY_SECONDS = 10L
internal const val MAX_CONSECUTIVE_ERRORS = 100
