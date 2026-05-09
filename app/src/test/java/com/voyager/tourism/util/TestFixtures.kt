package com.voyager.tourism.util

import com.voyager.tourism.data.dto.ConnectionRequestDto
import com.voyager.tourism.data.dto.CoordinatesDto
import com.voyager.tourism.data.dto.DestinationDto
import com.voyager.tourism.data.dto.TravelerMatchDto
import com.voyager.tourism.data.dto.TravelPlanDto
import com.voyager.tourism.data.dto.TravelPlanStatus
import com.voyager.tourism.data.dto.TripDto
import com.voyager.tourism.data.dto.LoginResponseDto
import com.voyager.tourism.data.dto.UserDto
import com.voyager.tourism.data.dto.UserRole
import com.voyager.tourism.data.dto.UserStatus
import com.voyager.tourism.domain.model.Coordinates
import com.voyager.tourism.domain.model.Destination
import com.voyager.tourism.domain.model.Trip
import com.voyager.tourism.domain.model.TripStatus
import com.voyager.tourism.domain.model.User

object TestFixtures {

    fun userDto(
        id: Long = 42L,
        token: String? = "jwt-token",
    ) = UserDto(
        id = id,
        username = "traveler",
        email = "t@example.com",
        firstName = "T",
        lastName = "R",
        phoneNumber = null,
        role = UserRole.TRAVELER,
        status = UserStatus.ACTIVE,
        profileImageUrl = null,
        bio = null,
        interests = setOf("museums"),
        dateOfBirth = null,
        createdAt = "2024-01-01T10:00:00Z",
        updatedAt = "2024-01-02T10:00:00Z",
        token = token,
    )

    fun loginResponseDto(
        user: UserDto = userDto(token = null),
        token: String = "jwt-token",
    ) = LoginResponseDto(
        token = token,
        tokenType = "Bearer",
        expiresIn = 7200L,
        user = user,
    )

    fun domainUser() = User(
        id = "42",
        email = "t@example.com",
        username = "traveler",
        firstName = "T",
        lastName = "R",
        phoneNumber = null,
        role = UserRole.TRAVELER.value,
        status = UserStatus.ACTIVE.value,
        profileImageUrl = null,
        bio = null,
        interests = setOf("museums"),
        dateOfBirth = null,
        createdAt = "2024-01-01T10:00:00Z",
        updatedAt = "2024-01-02T10:00:00Z",
        token = "jwt-token",
    )

    fun destinationDto() = DestinationDto(
        id = "d1",
        name = "Paris",
        country = "FR",
        coordinates = CoordinatesDto(48.8, 2.3),
        timezone = "CET",
        currency = "EUR",
        language = "fr",
        climate = "mild",
        bestTimeToVisit = "spring",
        averageCost = 100.0,
        rating = 4.5f,
        images = listOf("https://x/img.jpg"),
    )

    fun tripDto(status: String = "PLANNING") = TripDto(
        id = "trip-1",
        userId = "42",
        title = "Eurotrip",
        description = "Fun",
        destination = destinationDto(),
        startDate = 1_000L,
        endDate = 2_000L,
        budget = 500.0,
        travelers = 2,
        status = status,
        createdAt = 100L,
        updatedAt = 200L,
    )

    fun domainTrip() = Trip(
        id = "trip-1",
        userId = "42",
        title = "Eurotrip",
        description = "Fun",
        destination = Destination(
            id = "d1",
            name = "Paris",
            country = "FR",
            coordinates = Coordinates(48.8, 2.3),
            timezone = "CET",
            currency = "EUR",
            language = "fr",
            climate = "mild",
            bestTimeToVisit = "spring",
            averageCost = 100.0,
            rating = 4.5f,
            images = listOf("https://x/img.jpg"),
        ),
        startDate = 1_000L,
        endDate = 2_000L,
        budget = 500.0,
        travelers = 2,
        status = TripStatus.PLANNING,
        createdAt = 100L,
        updatedAt = 200L,
    )

    fun travelPlanDto(
        id: Long? = 99L,
        status: TravelPlanStatus? = TravelPlanStatus.DRAFT,
        destination: String? = "Lima",
        start: String? = "2025-06-01T10:00:00Z",
        end: String? = "2025-06-10T10:00:00Z",
    ) = TravelPlanDto(
        id = id,
        title = "Plan",
        description = "Desc",
        status = status,
        startDate = start,
        endDate = end,
        estimatedBudget = 300.0,
        actualCost = null,
        numberOfTravelers = 3,
        destinationLocation = destination,
        createdAt = start,
        updatedAt = end,
    )

    fun connectionRequest(
        id: Long = 1L,
        recipientId: Long = 2L,
        requesterId: Long = 3L,
        status: String = "PENDING",
    ) = ConnectionRequestDto(
        id = id,
        recipientId = recipientId,
        requesterId = requesterId,
        status = status,
        message = null,
        createdAt = "2026-01-01T00:00:00Z",
        updatedAt = "2026-01-01T00:00:00Z",
    )

    fun travelerMatch(userId: Long = 10L) = TravelerMatchDto(
        userId = userId,
        username = "match",
        firstName = "M",
        lastName = "T",
        profileImageUrl = null,
        bio = null,
        travelPlanId = 1L,
        travelPlanTitle = "Trip",
        destinationLocation = "Lima",
        travelStartDate = "2027-06-01",
        travelEndDate = "2027-06-10",
        numberOfTravelers = 2,
        daysOverlap = 3,
        compatibilityScore = 0.85,
    )
}
