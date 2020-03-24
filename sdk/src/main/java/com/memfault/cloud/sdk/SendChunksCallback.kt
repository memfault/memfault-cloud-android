package com.memfault.cloud.sdk

/**
 * A callback for [ChunkSender.send].
 *
 * One method will be called when there is no more data to send or when the request fails.
 *
 * The callback will not be run if [MemfaultCloud.deinit] has been called.
 *
 * The callback will be run on the main thread.
 */
interface SendChunksCallback : Callback {

    /**
     * An error occurred: the network may have been unavailable or there may have been an issue with
     * the request.
     *
     * @param sent the number of chunks that were successfully sent prior to the error.
     */
    fun onError(e: Exception, sent: Int)

    /**
     * The queue is now empty.
     *
     * @param sent the number of chunks that were sent due to this call.
     */
    fun onQueueEmpty(sent: Int)

    /**
     * The server was busy, please re-try after a minimum delay.
     *
     * @param delay the delay in seconds.
     * @param sent the number of chunks successfully sent prior to the busy response.
     */
    fun onRetryAfterDelay(delay: Long, sent: Int)
}
