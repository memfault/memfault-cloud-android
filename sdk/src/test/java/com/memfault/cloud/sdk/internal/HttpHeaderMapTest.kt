package com.memfault.cloud.sdk.internal

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HttpHeaderMapTest {
    @Test
    fun caseInsensitiveKeys() {
        val headers = HttpHeaderMap(mapOf("Retry-After" to "100"))
        assertEquals(true, headers.containsKey("RETRY-AFTER"))
        assertEquals("100", headers["retry-after"])
        assertEquals(setOf("retry-after"), headers.keys)
        assertEquals(mapOf("retry-after" to "100").entries, headers.entries)
    }
}
