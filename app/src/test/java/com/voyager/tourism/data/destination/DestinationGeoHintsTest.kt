package com.voyager.tourism.data.destination

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DestinationGeoHintsTest {

    @Test
    fun normalizeDestinationSlugForSearch_null_returnsEmpty() {
        assertEquals("", normalizeDestinationSlugForSearch(null))
    }

    @Test
    fun normalizeDestinationSlugForSearch_stripsDstPrefix() {
        assertEquals("barcelona beach", normalizeDestinationSlugForSearch("dst_barcelona_beach"))
    }

    @Test
    fun resolveDestinationHint_empty_returnsDefaultUnmatched() {
        val h = resolveDestinationHint("   ")
        assertFalse(h.matched)
        assertTrue(h.label.contains("Madrid"))
    }

    @Test
    fun resolveDestinationHint_matchesParis() {
        val h = resolveDestinationHint("Viaje a París")
        assertTrue(h.matched)
        assertEquals("PAR", h.cityCode)
    }

    @Test
    fun resolveDestinationHint_matchesCdmxAlias() {
        val h = resolveDestinationHint("cdmx food")
        assertTrue(h.matched)
        assertEquals("MEX", h.cityCode)
    }

    @Test
    fun buildDestinationExploreLabel_bothParts() {
        assertEquals("Lima, PE", buildDestinationExploreLabel("Lima", "PE"))
    }

    @Test
    fun buildDestinationExploreLabel_onlyCountry() {
        assertEquals("AR", buildDestinationExploreLabel("  ", "AR"))
    }

    @Test
    fun buildDestinationExploreLabel_fallbackDestino() {
        assertEquals("Destino", buildDestinationExploreLabel("", "   "))
    }

    @Test
    fun buildDestinationExploreLabel_slugFallbackForLoc() {
        assertEquals("barcelona, ES", buildDestinationExploreLabel("dst_barcelona", "ES"))
    }
}
