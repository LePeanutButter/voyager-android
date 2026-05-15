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
        assertEquals("MAD", m["origin_location_code"])
        assertEquals("BCN", m["destination_location_code"])
        assertEquals("2025-08-01", m["departure_date"])
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
        assertFalse(m.containsKey("return_date"))
        assertFalse(m.containsKey("travel_class"))
        assertFalse(m.containsKey("currency_code"))
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
        assertEquals("2025-01-10", m["return_date"])
        assertEquals("2", m["children"])
        assertEquals("5", m["max"])
        assertEquals("ECONOMY", m["travel_class"])
        assertEquals("true", m["non_stop"])
        assertEquals("EUR", m["currency_code"])
    }
}
