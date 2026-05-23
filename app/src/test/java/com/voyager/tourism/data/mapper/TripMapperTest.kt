package com.voyager.tourism.data.mapper

import com.voyager.tourism.data.database.entity.TripEntity
import com.voyager.tourism.data.dto.TravelPlanDto
import com.voyager.tourism.data.dto.TravelPlanStatus
import com.voyager.tourism.domain.model.TripStatus
import com.voyager.tourism.util.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TripMapperTest {

    private val mapper = TripMapper()

    @Test
    fun `fromTravelPlanDto maps status branches`() {
        val statuses = listOf(
            TravelPlanStatus.DRAFT to TripStatus.PLANNING,
            TravelPlanStatus.ACTIVE to TripStatus.ACTIVE,
            TravelPlanStatus.COMPLETED to TripStatus.COMPLETED,
            TravelPlanStatus.CANCELLED to TripStatus.CANCELLED,
            TravelPlanStatus.ON_HOLD to TripStatus.CONFIRMED,
            TravelPlanStatus.ARCHIVED to TripStatus.COMPLETED,
        )
        for ((dtoStatus, domain) in statuses) {
            val dto = TestFixtures.travelPlanDto(status = dtoStatus)
            val trip = mapper.fromTravelPlanDto(dto, "user-1")
            assertEquals(domain, trip.status)
        }
    }

    @Test
    fun `fromTravelPlanDto null status defaults planning`() {
        val trip = mapper.fromTravelPlanDto(TestFixtures.travelPlanDto(status = null), "u")
        assertEquals(TripStatus.PLANNING, trip.status)
    }

    @Test
    fun `fromTravelPlanDto blank destination uses default label`() {
        val dto = TestFixtures.travelPlanDto(destination = "")
        val trip = mapper.fromTravelPlanDto(dto, "u")
        assertEquals("Sin destino", trip.destination.name)
    }

    @Test
    fun `toDomain from TripDto invalid status defaults planning`() {
        val dto = TestFixtures.tripDto(status = "NOT_AN_ENUM")
        val trip = mapper.toDomain(dto)
        assertEquals(TripStatus.PLANNING, trip.status)
    }

    @Test
    fun `toDomain from TripEntity invalid status defaults planning`() {
        val entity = TripEntity(
            id = "1",
            userId = "u",
            title = "t",
            description = "d",
            destination = "X",
            startDate = 1L,
            endDate = 2L,
            budget = 1.0,
            travelers = 1,
            status = "???",
            itinerary = null,
            accommodations = null,
            activities = null,
            createdAt = 0L,
            updatedAt = 0L,
        )
        val trip = mapper.toDomain(entity)
        assertEquals(TripStatus.PLANNING, trip.status)
        assertEquals("X", trip.destination.name)
    }

    @Test
    fun `entity round trip via dto`() {
        val dto = TestFixtures.tripDto()
        val entity = mapper.toEntity(dto)
        assertEquals("Paris", entity.destination)
        val trip = mapper.toDomain(entity)
        assertEquals("Paris", trip.destination.name)
    }

    @Test
    fun `toTravelPlanDto maps trip status`() {
        val trip = TestFixtures.domainTrip().copy(status = TripStatus.ACTIVE)
        val plan = mapper.toTravelPlanDto(trip)
        assertEquals(TravelPlanStatus.ACTIVE, plan.status)
    }

    @Test
    fun `toTravelPlanDto maps all trip statuses to plan enums`() {
        val pairs = listOf(
            TripStatus.PLANNING to TravelPlanStatus.DRAFT,
            TripStatus.CONFIRMED to TravelPlanStatus.ON_HOLD,
            TripStatus.ACTIVE to TravelPlanStatus.ACTIVE,
            TripStatus.COMPLETED to TravelPlanStatus.COMPLETED,
            TripStatus.CANCELLED to TravelPlanStatus.CANCELLED,
        )
        for ((dom, wire) in pairs) {
            val plan = mapper.toTravelPlanDto(TestFixtures.domainTrip().copy(status = dom))
            assertEquals(wire, plan.status)
        }
    }

    @Test
    fun `fromTravelPlanDto parses offset and local date strings`() {
        val withOffset = TravelPlanDto(
            startDate = "2027-03-01T10:15:30Z",
            endDate = "2027-03-05T10:15:30Z",
        )
        val t1 = mapper.fromTravelPlanDto(withOffset, "u")
        assertTrue(t1.startDate > 0L)

        val localOnly = TravelPlanDto(
            startDate = "2027-04-01T08:00:00",
            endDate = "2027-04-10T08:00:00",
        )
        val t2 = mapper.fromTravelPlanDto(localOnly, "u")
        assertTrue(t2.endDate >= t2.startDate)
    }
}
