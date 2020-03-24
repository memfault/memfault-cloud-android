package com.memfault.cloud.sdk

/**
 * An API that allows a [ChunkSender] to queue chunks into batched requests.
 */
interface ChunkQueue {

    /**
     * Enqueue the chunks; return false if not successful.
     */
    fun addChunks(chunks: List<ByteArray>): Boolean

    /**
     * Return a list with at most the first `count` items from the head of the queue.
     */
    fun peek(count: Int): List<ByteArray>

    /**
     * Remove at most the first `count` items from head of the queue.
     */
    fun drop(count: Int)
}
