package com.memfault.cloud.sdk.internal

import com.memfault.cloud.sdk.ExponentialBackoff
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.math.pow

private const val BASE_DELAY = 10.0
private const val FACTOR = 2.0

class ExponentialBackoffTest {
    lateinit var backoff: ExponentialBackoff

    @BeforeEach
    fun setUp() {
        backoff = ExponentialBackoff(BASE_DELAY, 2, factor = FACTOR)
    }

    @Test
    fun initialDelay() {
        assertEquals(0.0, backoff.delay)
    }

    @Test
    fun errors() {
        backoff.trackError()
        assertEquals(FACTOR.pow(0) * BASE_DELAY, backoff.delay)
        backoff.trackError()
        assertEquals(FACTOR.pow(1) * BASE_DELAY, backoff.delay)
        backoff.trackError()
        assertEquals(FACTOR.pow(2) * BASE_DELAY, backoff.delay)

        // maxExponent reached:
        backoff.trackError()
        assertEquals(FACTOR.pow(2) * BASE_DELAY, backoff.delay)
    }

    @Test
    fun success() {
        backoff.trackError()
        backoff.trackSuccess()
        assertEquals(0.0, backoff.delay)
    }

    @Test
    fun copy() {
        backoff.trackError()
        val newBaseDelay = 2 * BASE_DELAY
        val backoffCopy = backoff.copy(baseDelay = newBaseDelay)
        assertEquals(backoffCopy.baseDelay, newBaseDelay)
        assertEquals(backoffCopy.exponent, backoff.exponent)
        assertEquals(backoffCopy.factor, backoff.factor)
        assertEquals(backoffCopy.delay, FACTOR.pow(0) * newBaseDelay)
    }
}
