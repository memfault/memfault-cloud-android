package com.memfault.cloud.sdk.internal

import com.memfault.cloud.sdk.ChunkQueue
import com.memfault.cloud.sdk.PostChunksCallback
import java.util.concurrent.Executor

internal class PostChunksTask(
    private val memfaultHttpApi: MemfaultHttpApi,
    private val executor: Executor,
    private val deviceSerial: String,
    private val chunkQueue: ChunkQueue,
    private val callback: PostChunksCallback,
    private val maxChunksPerRequest: Int = 1000
) : Runnable {
    override fun run() {
        var chunksSent = 0
        while (true) {
            val chunksToSend = chunkQueue.peek(maxChunksPerRequest)
            if (chunksToSend.isEmpty()) break
            val response = memfaultHttpApi.postChunks(deviceSerial, chunksToSend)
            if (response.isSucessful()) {
                chunksSent += chunksToSend.size
                chunkQueue.drop(chunksToSend.size)
                continue
            }
            val errorRunnable = when {
                (response == null) -> Runnable {
                    callback.onError(
                        Exception("Request failed $response"),
                        chunksSent
                    )
                }
                (response.code == 503) -> {
                    val retryAfter: String? = response.headers["Retry-After"]
                    val retryAfterLong = retryAfter?.toLong()
                    if (retryAfterLong == null) {
                        Runnable {
                            callback.onError(
                                Exception("Failed to determine Retry-After time $response"),
                                chunksSent
                            )
                        }
                    } else {
                        Runnable {
                            callback.onRetryAfterDelay(retryAfterLong, chunksSent)
                        }
                    }
                }
                else -> Runnable {
                    callback.onError(Exception("Request failed $response"), chunksSent)
                }
            }
            executor.execute(errorRunnable)
            return
        }
        executor.execute {
            callback.onQueueEmpty(chunksSent)
        }
    }
}
