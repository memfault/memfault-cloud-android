package com.memfault.cloud.sdk

import com.memfault.cloud.sdk.ChunkSender.Builder
import com.memfault.cloud.sdk.internal.PostChunksTask
import com.memfault.cloud.sdk.internal.TemporaryChunkQueue

/**
 * Send chunks for a given device to Memfault's web services.
 *
 * One chunk sender must be created for each device; all chunk senders can then share
 * the same [MemfaultCloud] instance.
 *
 * ## Initialization
 *
 * Use the [Builder] to instantiate a [ChunkSender]:
 *
 * Kotlin:
 *
 * ```kotlin
 * val chunkSender = ChunkSender.Builder()
 *   .setMemfaultCloud(memfaultCloud)
 *   .setDeviceSerialNumber(deviceSerial)
 *   .build()
 * ```
 *
 * Java:
 *
 * ```java
 * final ChunkSender chunkSender = new ChunkSender.Builder()
 *   .setMemfaultCloud(memfaultCloud)
 *   .setDeviceSerialNumber(deviceSerial)
 *   .build()
 * ```
 *
 * ## Persistent Queuing
 *
 * By default, the [ChunkSender]'s queue lives in memory. If the application is closed or
 * crashes with chunks un-sent, they will be lost.
 *
 * If this is an issue for your application, you can provide the [ChunkSender] with a
 * [ChunkQueue] that is persistent.
 */
class ChunkSender internal constructor(
    private val memfaultCloud: MemfaultCloud,
    private val deviceSerial: String,
    private val chunkQueue: ChunkQueue,
    private var maxChunksPerRequest: Int,
    private val errorTracker: ChunkErrorTracker
) {
    /**
     * Build a [ChunkSender] instance for a given device.
     *
     * Calling [setMemfaultCloud] and [setDeviceSerialNumber] is required before calling [build].
     */
    class Builder {
        private var memfaultCloud: MemfaultCloud? = null
        private var deviceSerial: String? = null
        private var chunkQueue: ChunkQueue? = null
        private var maxChunksPerRequest: Int = MAX_CHUNKS_PER_REQUEST

        /**
         * Provide a custom [ChunkQueue] that is managed by the caller.
         *
         * Use this API if you wish to provide a persistent [ChunkQueue] implementation.
         *
         * Only clear items from the [chunkQueue] after calling [MemfaultCloud.deinit] to avoid
         * concurrency issues with any work that is currently in-flight.
         *
         * It is safe to add items to the queue at any time.
         */
        fun setChunkQueue(chunkQueue: ChunkQueue): Builder {
            this.chunkQueue = chunkQueue
            return this
        }

        /**
         * Provide an active instance of the API to the [ChunkSender].
         *
         * You only need to create one [MemfaultCloud]; the same instance can be passed into several
         * instances of [ChunkSender].
         */
        fun setMemfaultCloud(memfaultCloud: MemfaultCloud): Builder {
            this.memfaultCloud = memfaultCloud
            return this
        }

        /**
         * Provide the serial number of the device that produced the chunks.
         */
        fun setDeviceSerialNumber(deviceSerial: String): Builder {
            this.deviceSerial = deviceSerial
            return this
        }

        fun setMaxChunksPerRequest(maxChunksPerRequest: Int): Builder {
            this.maxChunksPerRequest = minOf(MAX_CHUNKS_PER_REQUEST, maxChunksPerRequest)
            return this
        }

        /**
         * Create a [ChunkSender] instance using the configured values.
         */
        fun build(): ChunkSender = ChunkSender(
            memfaultCloud = checkNotNull(memfaultCloud) { "Memfault API must not be null" },
            deviceSerial = checkNotNull(deviceSerial) { "Device serial number must not be null" },
            chunkQueue = chunkQueue ?: TemporaryChunkQueue(),
            maxChunksPerRequest = maxChunksPerRequest,
            errorTracker = ChunkErrorTracker()
        )
    }

    /**
     * Enqueue the chunks for the given device.
     *
     * The chunks will be added to the [ChunkQueue].
     *
     * @param chunks the chunks to be enqueued and sent to the server.
     */
    fun addChunks(chunks: List<ByteArray>) = chunkQueue.addChunks(chunks)

    /**
     * Attempt to drain all chunks for the given device.
     *
     * No chunks will be sent and the callback will not be executed if the [MemfaultCloud]'s
     * [MemfaultCloud.deinit] method has been called.
     *
     * @param callback called when there is no more data to send or a request fails.
     */
    fun send(callback: SendChunksCallback) {
        memfaultCloud.workerThreadExecutor.execute(
            PostChunksTask(
                memfaultHttpApi = memfaultCloud.buildMemfaultHttpApi(),
                executor = memfaultCloud.mainThreadExecutor,
                deviceSerial = deviceSerial,
                chunkQueue = chunkQueue,
                callback = callback,
                maxChunksPerRequest = maxChunksPerRequest,
                errorTracker = errorTracker
            )
        )
    }
}

private const val MAX_CHUNKS_PER_REQUEST = 100
