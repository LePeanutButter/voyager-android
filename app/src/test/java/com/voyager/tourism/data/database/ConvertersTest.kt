package com.voyager.tourism.data.database

import com.voyager.tourism.data.dto.CoordinatesDto
import com.voyager.tourism.data.dto.DestinationDto
import com.voyager.tourism.data.dto.UserPreferencesDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun stringList_roundTrip() {
        val list = listOf("a", "b")
        val json = converters.fromStringList(list)
        assertEquals(list, converters.toStringList(json))
    }

    @Test
    fun `toStringList empty json array`() {
        assertTrue(converters.toStringList("[]").isEmpty())
    }

    @Test
    fun destinationDto_roundTrip() {
        val d = DestinationDto(
            id = "1",
            name = "N",
            country = "C",
            coordinates = CoordinatesDto(1.0, 2.0),
            timezone = "t",
            currency = "cur",
            language = "en",
            climate = "c",
            bestTimeToVisit = "b",
            averageCost = 1.0,
            rating = 3f,
        )
        val s = converters.fromDestinationDto(d)
        assertEquals(d.name, converters.toDestinationDto(s).name)
    }

    @Test(expected = java.io.EOFException::class)
    fun `toDestinationDto malformed json throws`() {
        converters.toDestinationDto("{")
    }

    @Test
    fun userPreferences_roundTrip() {
        val p = UserPreferencesDto(preferredDestinations = listOf("Lima"))
        val s = converters.fromUserPreferencesDto(p)
        assertEquals("Lima", converters.toUserPreferencesDto(s).preferredDestinations.first())
    }
}
