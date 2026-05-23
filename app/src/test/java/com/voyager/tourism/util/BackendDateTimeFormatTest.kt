package com.voyager.tourism.util

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale

class BackendDateTimeFormatTest {

    private lateinit var defaultLocale: Locale

    @Before
    fun setUp() {
        defaultLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
    }

    @After
    fun tearDown() {
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `formatBackendLocalDateTime returns formatted string when valid`() {
        val input = "2026-05-06T15:30:00"
        val pattern = "MMM d, yyyy"
        val expected = "May 6, 2026"
        assertEquals(expected, formatBackendLocalDateTime(input, pattern))
    }

    @Test
    fun `formatBackendLocalDateTime returns original string when invalid`() {
        val input = "not-a-date"
        assertEquals(input, formatBackendLocalDateTime(input, "yyyy"))
    }

    @Test
    fun `formatBackendLocalDateTime returns formatted string with different pattern`() {
        val input = "2026-12-31T23:59:59"
        val pattern = "dd/MM/yyyy HH:mm"
        val expected = "31/12/2026 23:59"
        assertEquals(expected, formatBackendLocalDateTime(input, pattern))
    }
}
