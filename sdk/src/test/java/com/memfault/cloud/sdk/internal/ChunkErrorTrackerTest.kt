package com.memfault.cloud.sdk.internal

import com.memfault.cloud.sdk.ChunkErrorTracker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class ChunkErrorTrackerTest {
    private lateinit var chunkErrorTracker: ChunkErrorTracker
    private var nowMillis: Long = 0

    @BeforeEach
    fun setUp() {
        chunkErrorTracker =
            ChunkErrorTracker {
                nowMillis
            }
    }

    @Test
    fun consecutiveErrorCount() {
        assertEquals(0, chunkErrorTracker.consecutiveErrorCount)
        chunkErrorTracker.trackError()
        chunkErrorTracker.trackError()
        assertEquals(2, chunkErrorTracker.consecutiveErrorCount)
        chunkErrorTracker.trackSuccess()
        assertEquals(0, chunkErrorTracker.consecutiveErrorCount)
    }

    @Test
    fun nextDelay() {
        val baseDelay = 5L
        chunkErrorTracker.baseDelaySeconds = baseDelay
        chunkErrorTracker.trackError()
        assertEquals(baseDelay, chunkErrorTracker.nextDelay())
        nowMillis += 1000
        assertEquals(baseDelay - 1, chunkErrorTracker.nextDelay())
        // Rounds up:
        nowMillis += 3999
        assertEquals(1, chunkErrorTracker.nextDelay())
        nowMillis += 1
        assertEquals(null, chunkErrorTracker.nextDelay())
        nowMillis += 1
        assertEquals(null, chunkErrorTracker.nextDelay())
    }

    @Test
    fun nextDelayNullAfterSuccess() {
        chunkErrorTracker.trackError()
        assertNotNull(chunkErrorTracker.nextDelay())

        chunkErrorTracker.trackSuccess()
        assertEquals(null, chunkErrorTracker.nextDelay())
    }
}
