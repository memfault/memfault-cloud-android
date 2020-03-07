package com.memfault.cloud.sdk

import com.memfault.cloud.sdk.MemfaultChunkSender.Builder
import com.memfault.cloud.sdk.internal.Logger
import com.memfault.cloud.sdk.internal.PostChunksTask
import com.memfault.cloud.sdk.internal.TemporaryChunkQueue

/**
 * An API for sending chunks for a given device to Memfault's web services.
 *
 * One chunk sender must be created for each device; all chunk senders can the share
 * the same [MemfaultApi] instance.
 *
 * ## Initialization
 *
 * Use the [Builder] to instantiate a [MemfaultChunkSender]:
 *
 * Kotlin:
 *
 * ```kotlin
 * val chunkSender = MemfaultChunkSender.Builder()
 *   .setMemfaultApi(memfaultApi)
 *   .setDeviceSerialNumber(deviceSerial)
 *   .build()
 * ```
 *
 * Java:
 *
 * ```java
 * final MemfaultChunkSender chunkSender = new MemfaultChunkSender.Builder()
 *   .setMemfaultApi(memfaultApi)
 *   .setDeviceSerialNumber(deviceSerial)
 *   .build()
 * ```
 *
 * ## De-initialization
 *
 * Call [stop] prior to [MemfaultApi.deinit] to safely shut down the sender.
 *
 * ## Persistent Queuing
 *
 * By default, the [MemfaultChunkSender]'s queue lives in memory. If the application is closed or
 * crashes with chunks un-sent, they will be lost.
 *
 * If this is an issue for your application, you can provide the [MemfaultChunkSender] with a
 * [ChunkQueue] that is persistent.
 */
class MemfaultChunkSender private constructor(
    private val memfaultApi: MemfaultApi,
    private val deviceSerial: String,
    private val chunkQueue: ChunkQueue
) {
    /**
     * Build a [MemfaultChunkSender] instance for a given device.
     *
     * Calling [setMemfaultApi] and [setDeviceSerialNumber] is required before calling [build].
     */
    class Builder {
        private var memfaultApi: MemfaultApi? = null
        private var deviceSerial: String? = null
        private var chunkQueue: ChunkQueue = TemporaryChunkQueue()

        /**
         * Provide a custom [ChunkQueue] that is managed by the caller.
         *
         * This is useful if you wish to provide a persistent implementation.
         *
         * When a [ChunkQueue] is provided, it is *not* cleared by the [MemfaultApi] upon
         * de-initialization.
         *
         * Only clear items from the [ChunkQueue] after calling [MemfaultApi.deinit]] to avoid
         * concurrency issues with any work that is currently in-flight.
         *
         * It is safe to add items to the queue at any time.
         */
        fun setChunkQueue(chunkQueue: ChunkQueue): Builder {
            this.chunkQueue = chunkQueue
            return this
        }

        /**
         * Provide an active instance of the API to the [MemfaultChunkSender].
         *
         * You only need to create one [MemfaultApi]; the same instance can be passed into several
         * instances of [MemfaultChunkSender].
         */
        fun setMemfaultApi(memfaultApi: MemfaultApi): Builder {
            this.memfaultApi = memfaultApi
            return this
        }

        /**
         * Provide the serial number of the device that produced the chunks.
         */
        fun setDeviceSerialNumber(deviceSerial: String): Builder {
            this.deviceSerial = deviceSerial
            return this
        }

        /**
         * Create a [MemfaultChunkSender] instance using the configured values.
         */
        fun build(): MemfaultChunkSender = MemfaultChunkSender(
            memfaultApi = checkNotNull(memfaultApi) { "Memfault API must not be null" },
            deviceSerial = checkNotNull(deviceSerial) { "Device serial number must not be null" },
            chunkQueue = chunkQueue
        )
    }

    /**
     * Enqueue the chunks and and attempt to drain all chunks for the given device.
     *
     * Calling this method with an empty list of chunks will trigger a request to the server
     * that contains chunks from the device that are still enqueued.
     *
     * @param chunks the chunks to be enqueued and sent to the server.
     * @param callback called when there is no more data to send or a request fails.
     */
    fun postChunks(chunks: List<ByteArray>, callback: PostChunksCallback) {
        chunkQueue.addChunks(chunks)
        memfaultApi.networkThreadExecutor.execute(
            PostChunksTask(
                memfaultHttpApi = memfaultApi.buildMemfaultHttpApi(),
                executor = memfaultApi.mainThreadExecutor,
                deviceSerial = deviceSerial,
                chunkQueue = chunkQueue,
                callback = callback
            )
        )
    }

    /**
     * Stop sending chunks for this device. You must call [stop] before [MemfaultApi.deinit].
     *
     * If the default [ChunkQueue] implementation is used, it will be cleared.
     */
    fun stop() {
        if (chunkQueue is TemporaryChunkQueue) {
            memfaultApi.networkThreadExecutor.execute {
                Logger.i("Clearing unsent chunks")
                chunkQueue.clear()
            }
        }
    }
}
