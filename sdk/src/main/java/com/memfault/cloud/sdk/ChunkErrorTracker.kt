package com.memfault.cloud.sdk

import android.os.SystemClock.elapsedRealtime
import kotlin.math.ceil

class ChunkErrorTracker(
    private var backoff: ExponentialBackoff = ExponentialBackoff(1.0, maxExponent = 10),
    private val getElapsedRealtimeMillis: () -> Long = ::elapsedRealtime
) {
    var baseDelaySeconds: Long
        get() = backoff.baseDelay.toLong()
        set(value) {
            backoff = backoff.copy(baseDelay = value.toDouble())
        }

    private var lastErrorRealtimeMillis: Long? = null

    var consecutiveErrorCount: Int = 0
        private set

    fun trackError(): Int {
        backoff.trackError()
        lastErrorRealtimeMillis = getElapsedRealtimeMillis()
        consecutiveErrorCount += 1
        return consecutiveErrorCount
    }

    fun trackSuccess() {
        backoff.trackSuccess()
        lastErrorRealtimeMillis = null
        consecutiveErrorCount = 0
    }

    fun nextDelay(): Long? {
        val last = lastErrorRealtimeMillis ?: return null
        val now = getElapsedRealtimeMillis()
        val earliest = last + backoff.delay * MILLIS_IN_SECOND
        val delay = earliest - now
        if (delay <= 0) return null
        return ceil(delay / MILLIS_IN_SECOND.toDouble()).toLong()
    }
}

private const val MILLIS_IN_SECOND = 1000L
