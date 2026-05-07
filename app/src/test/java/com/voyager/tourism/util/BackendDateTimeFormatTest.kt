package com.voyager.tourism.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackendDateTimeFormatTest {

    @Test
    fun `formats valid iso local date time`() {
        val out = formatBackendLocalDateTime("2025-06-15T14:30:00", "yyyy-MM-dd")
        assertEquals("2025-06-15", out)
    }

    @Test
    fun `returns original string when parse fails`() {
        val bad = "not-a-date"
        assertEquals(bad, formatBackendLocalDateTime(bad, "yyyy-MM-dd"))
    }

    @Test
    fun `pattern with literals is applied`() {
        val out = formatBackendLocalDateTime("2025-01-02T10:00:00", "dd/MM/yyyy")
        assertTrue(out.contains("02"))
        assertTrue(out.contains("01"))
        assertTrue(out.contains("2025"))
    }
}
