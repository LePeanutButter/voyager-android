package com.voyager.tourism.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FlightOffersQueryTest {

    @Test
    fun `toQueryMap includes required keys and defaults`() {
        val q = FlightOffersQuery(
            originLocationCode = "MAD",
            destinationLocationCode = "BCN",
            departureDate = "2025-08-01",
        )
        val m = q.toQueryMap()
        assertEquals("MAD", m["originLocationCode"])
        assertEquals("BCN", m["destinationLocationCode"])
        assertEquals("2025-08-01", m["departureDate"])
        assertEquals("1", m["adults"])
    }

    @Test
    fun `optional blank strings omitted`() {
        val q = FlightOffersQuery(
            originLocationCode = "A",
            destinationLocationCode = "B",
            departureDate = "2025-01-01",
            returnDate = "   ",
            travelClass = "",
            currencyCode = " ",
        )
        val m = q.toQueryMap()
        assertFalse(m.containsKey("returnDate"))
        assertFalse(m.containsKey("travelClass"))
        assertFalse(m.containsKey("currencyCode"))
    }

    @Test
    fun `optional non blank and flags included`() {
        val q = FlightOffersQuery(
            originLocationCode = "A",
            destinationLocationCode = "B",
            departureDate = "2025-01-01",
            returnDate = "2025-01-10",
            children = 2,
            max = 5,
            travelClass = "ECONOMY",
            nonStop = true,
            currencyCode = "EUR",
        )
        val m = q.toQueryMap()
        assertEquals("2025-01-10", m["returnDate"])
        assertEquals("2", m["children"])
        assertEquals("5", m["max"])
        assertEquals("ECONOMY", m["travelClass"])
        assertEquals("true", m["nonStop"])
        assertEquals("EUR", m["currencyCode"])
    }
}
