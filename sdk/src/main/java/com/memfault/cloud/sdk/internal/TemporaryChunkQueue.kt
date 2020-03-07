package com.memfault.cloud.sdk.internal

import com.memfault.cloud.sdk.ChunkQueue
import java.util.concurrent.ConcurrentLinkedQueue

class TemporaryChunkQueue(
    private val chunks: ConcurrentLinkedQueue<ByteArray> = ConcurrentLinkedQueue()
) : ChunkQueue {

    override fun addChunks(chunks: List<ByteArray>): Boolean = this.chunks.addAll(chunks)

    override fun peek(count: Int): List<ByteArray> {
        val result = mutableListOf<ByteArray>()
        var i = 0
        for (chunk in chunks) {
            if (i >= count) break
            result.add(chunk)
            i++
        }
        return result.toList()
    }

    override fun drop(count: Int) {
        var i = count
        while (i > 0 && chunks.isNotEmpty()) {
            chunks.poll()
            i--
        }
    }

    fun clear() = this.chunks.clear()
}
