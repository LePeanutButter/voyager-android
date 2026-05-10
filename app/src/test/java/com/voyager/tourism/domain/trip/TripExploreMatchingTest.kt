package com.voyager.tourism.domain.trip

import com.voyager.tourism.domain.model.Coordinates
import com.voyager.tourism.domain.model.Destination
import com.voyager.tourism.domain.model.Trip
import com.voyager.tourism.domain.model.TripStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TripExploreMatchingTest {

    private fun trip(destName: String, title: String = "Viaje", destId: String = "dst_1") = Trip(
        id = "1",
        userId = "u",
        title = title,
        description = "",
        destination = Destination(
            id = destId,
            name = destName,
            country = "CO",
            coordinates = Coordinates(0.0, 0.0),
            timezone = "UTC",
            currency = "COP",
            language = "es",
            climate = "",
            bestTimeToVisit = "",
            averageCost = 0.0,
            rating = 4f,
        ),
        startDate = 0L,
        endDate = 0L,
        budget = 0.0,
        travelers = 1,
        status = TripStatus.PLANNING,
        createdAt = 0L,
        updatedAt = 0L,
    )

    @Test
    fun primaryPlaceToken_takesBeforeComma() {
        assertEquals("bariloche", primaryPlaceToken("Bariloche, AR"))
    }

    @Test
    fun tripMatchesDestinationLabel_byCityName() {
        val t = trip("San Carlos de Bariloche")
        assertTrue(tripMatchesDestinationLabel(t, "Bariloche, Argentina"))
    }

    @Test
    fun tripMatchesDestinationLabel_byDstSlug() {
        val t = trip("X", destId = "dst_bariloche")
        assertTrue(tripMatchesDestinationLabel(t, "bariloche"))
    }

    @Test
    fun tripMatchesDestinationLabel_noFalsePositive() {
        val t = trip("Oaxaca")
        assertFalse(tripMatchesDestinationLabel(t, "Patagonia"))
    }

    @Test
    fun tripMatchesDestinationLabel_blankPrimary_returnsFalse() {
        val t = trip("X")
        assertFalse(tripMatchesDestinationLabel(t, "   , AR"))
    }

    @Test
    fun tripMatchesDestinationLabel_byTitle() {
        val t = trip("Otro", title = "Escapada a Mendoza")
        assertTrue(tripMatchesDestinationLabel(t, "Mendoza, AR"))
    }

    @Test
    fun tripMatchesDestinationLabel_byCountry() {
        val t = trip("X", destId = "dst_x")
        val trip = t.copy(
            destination = t.destination.copy(name = "Capital", country = "Argentina"),
        )
        assertTrue(tripMatchesDestinationLabel(trip, "argentina"))
    }

    @Test
    fun tripMatchesDestinationLabel_byDestinationIdEqualsPrimary() {
        val t = trip("Y", destId = "Bariloche")
        assertTrue(tripMatchesDestinationLabel(t, "bariloche"))
    }
}
