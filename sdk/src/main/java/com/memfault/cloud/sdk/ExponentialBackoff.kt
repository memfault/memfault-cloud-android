package com.memfault.cloud.sdk

import kotlin.math.pow

class ExponentialBackoff(
    var baseDelay: Double,
    val maxExponent: Int,
    initialExponent: Int = NO_DELAY_EXPONENT,
    val factor: Double = 2.0,
) {
    init {
        require(baseDelay > 0.0)
    }

    var exponent: Int = initialExponent
        private set

    val delay: Double
        get() = if (exponent == NO_DELAY_EXPONENT) 0.0 else factor.pow(exponent) * baseDelay

    fun trackError() {
        exponent = minOf(exponent + 1, maxExponent)
    }

    fun trackSuccess() {
        exponent = NO_DELAY_EXPONENT
    }

    fun copy(baseDelay: Double) =
        ExponentialBackoff(
            baseDelay = baseDelay,
            maxExponent = maxExponent,
            initialExponent = exponent,
            factor = factor,
        )
}

private const val NO_DELAY_EXPONENT = -1
