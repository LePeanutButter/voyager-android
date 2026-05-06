package com.voyager.tourism.data.mapper

import com.voyager.tourism.data.database.entity.TripEntity
import com.voyager.tourism.data.dto.CoordinatesDto
import com.voyager.tourism.data.dto.DestinationDto
import com.voyager.tourism.data.dto.TripDto
import com.voyager.tourism.data.dto.TravelPlanDto
import com.voyager.tourism.data.dto.TravelPlanStatus
import com.voyager.tourism.domain.model.Coordinates
import com.voyager.tourism.domain.model.Destination
import com.voyager.tourism.domain.model.Trip
import com.voyager.tourism.domain.model.TripStatus
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mapper for converting between different Trip representations
 * Handles conversion between domain model, DTO, and entity
 */
@Singleton
class TripMapper @Inject constructor() {

    fun fromTravelPlanDto(dto: TravelPlanDto, fallbackUserId: String): Trip {
        val destLabel = dto.destinationLocation.orEmpty().ifBlank { "Sin destino" }
        val start = parseInstantMillis(dto.startDate) ?: System.currentTimeMillis()
        val end = parseInstantMillis(dto.endDate) ?: start
        return Trip(
            id = dto.id?.toString() ?: "",
            userId = fallbackUserId,
            title = dto.title.orEmpty(),
            description = dto.description.orEmpty(),
            destination = Destination(
                id = destLabel,
                name = destLabel,
                country = "",
                coordinates = Coordinates(0.0, 0.0),
                timezone = "",
                currency = "",
                language = "",
                climate = "",
                bestTimeToVisit = "",
                averageCost = 0.0,
                rating = 0f,
                images = emptyList(),
            ),
            startDate = start,
            endDate = end,
            budget = dto.estimatedBudget ?: dto.actualCost ?: 0.0,
            travelers = dto.numberOfTravelers ?: 1,
            status = mapPlanStatus(dto.status),
            itinerary = emptyList(),
            accommodations = emptyList(),
            activities = emptyList(),
            createdAt = parseInstantMillis(dto.createdAt) ?: start,
            updatedAt = parseInstantMillis(dto.updatedAt) ?: end,
        )
    }

    private fun mapPlanStatus(status: TravelPlanStatus?): TripStatus = when (status) {
        TravelPlanStatus.DRAFT -> TripStatus.PLANNING
        TravelPlanStatus.ACTIVE -> TripStatus.ACTIVE
        TravelPlanStatus.COMPLETED -> TripStatus.COMPLETED
        TravelPlanStatus.CANCELLED -> TripStatus.CANCELLED
        TravelPlanStatus.ON_HOLD -> TripStatus.CONFIRMED
        TravelPlanStatus.ARCHIVED -> TripStatus.COMPLETED
        null -> TripStatus.PLANNING
    }

    fun toTravelPlanDto(trip: Trip): TravelPlanDto {
        return TravelPlanDto(
            id = trip.id.toLongOrNull().takeIf { it != 0L },
            title = trip.title,
            description = trip.description,
            status = mapTripStatusToPlan(trip.status),
            travelType = com.voyager.tourism.data.dto.TravelType.LEISURE,
            startDate = millisToOffsetString(trip.startDate),
            endDate = millisToOffsetString(trip.endDate),
            estimatedBudget = trip.budget,
            numberOfTravelers = trip.travelers,
            originLocation = null,
            destinationLocation = trip.destination.name,
        )
    }

    private fun millisToOffsetString(epoch: Long): String =
        OffsetDateTime.ofInstant(java.time.Instant.ofEpochMilli(epoch), ZoneOffset.UTC).toString()

    private fun mapTripStatusToPlan(status: TripStatus): TravelPlanStatus = when (status) {
        TripStatus.PLANNING -> TravelPlanStatus.DRAFT
        TripStatus.CONFIRMED -> TravelPlanStatus.ON_HOLD
        TripStatus.ACTIVE -> TravelPlanStatus.ACTIVE
        TripStatus.COMPLETED -> TravelPlanStatus.COMPLETED
        TripStatus.CANCELLED -> TravelPlanStatus.CANCELLED
    }

    private fun parseInstantMillis(value: String?): Long? {
        if (value.isNullOrBlank()) return null
        return try {
            OffsetDateTime.parse(value).toInstant().toEpochMilli()
        } catch (_: Exception) {
            try {
                java.time.LocalDateTime.parse(value).toInstant(ZoneOffset.UTC).toEpochMilli()
            } catch (_: Exception) {
                null
            }
        }
    }
    
    fun toDomain(tripDto: TripDto): Trip {
        return Trip(
            id = tripDto.id,
            userId = tripDto.userId,
            title = tripDto.title,
            description = tripDto.description,
            destination = destinationDtoToDomain(tripDto.destination),
            startDate = tripDto.startDate,
            endDate = tripDto.endDate,
            budget = tripDto.budget,
            travelers = tripDto.travelers,
            status = runCatching { TripStatus.valueOf(tripDto.status) }.getOrDefault(TripStatus.PLANNING),
            itinerary = emptyList(),
            accommodations = emptyList(),
            activities = emptyList(),
            createdAt = tripDto.createdAt,
            updatedAt = tripDto.updatedAt,
        )
    }

    fun toDomain(tripEntity: TripEntity): Trip {
        val label = tripEntity.destination
        return Trip(
            id = tripEntity.id,
            userId = tripEntity.userId,
            title = tripEntity.title,
            description = tripEntity.description,
            destination = Destination(
                id = label,
                name = label,
                country = "",
                coordinates = Coordinates(0.0, 0.0),
                timezone = "",
                currency = "",
                language = "",
                climate = "",
                bestTimeToVisit = "",
                averageCost = 0.0,
                rating = 0f,
                images = emptyList(),
            ),
            startDate = tripEntity.startDate,
            endDate = tripEntity.endDate,
            budget = tripEntity.budget,
            travelers = tripEntity.travelers,
            status = runCatching { TripStatus.valueOf(tripEntity.status) }.getOrDefault(TripStatus.PLANNING),
            itinerary = emptyList(),
            accommodations = emptyList(),
            activities = emptyList(),
            createdAt = tripEntity.createdAt,
            updatedAt = tripEntity.updatedAt,
        )
    }

    fun toDto(trip: Trip): TripDto {
        return TripDto(
            id = trip.id,
            userId = trip.userId,
            title = trip.title,
            description = trip.description,
            destination = destinationDomainToDto(trip.destination),
            startDate = trip.startDate,
            endDate = trip.endDate,
            budget = trip.budget,
            travelers = trip.travelers,
            status = trip.status.name,
            createdAt = trip.createdAt,
            updatedAt = trip.updatedAt,
        )
    }

    fun toEntity(tripDto: TripDto): TripEntity {
        return TripEntity(
            id = tripDto.id,
            userId = tripDto.userId,
            title = tripDto.title,
            description = tripDto.description,
            destination = tripDto.destination.name,
            startDate = tripDto.startDate,
            endDate = tripDto.endDate,
            budget = tripDto.budget,
            travelers = tripDto.travelers,
            status = tripDto.status,
            itinerary = null,
            accommodations = null,
            activities = null,
            createdAt = tripDto.createdAt,
            updatedAt = tripDto.updatedAt,
        )
    }

    fun toEntityFromTrip(trip: Trip): TripEntity = toEntity(toDto(trip))

    private fun destinationDtoToDomain(d: DestinationDto): Destination {
        return Destination(
            id = d.id,
            name = d.name,
            country = d.country,
            coordinates = Coordinates(d.coordinates.latitude, d.coordinates.longitude),
            timezone = d.timezone,
            currency = d.currency,
            language = d.language,
            climate = d.climate,
            bestTimeToVisit = d.bestTimeToVisit,
            averageCost = d.averageCost,
            rating = d.rating,
            images = d.images,
        )
    }

    private fun destinationDomainToDto(d: Destination): DestinationDto {
        return DestinationDto(
            id = d.id,
            name = d.name,
            country = d.country,
            coordinates = CoordinatesDto(d.coordinates.latitude, d.coordinates.longitude),
            timezone = d.timezone,
            currency = d.currency,
            language = d.language,
            climate = d.climate,
            bestTimeToVisit = d.bestTimeToVisit,
            averageCost = d.averageCost,
            rating = d.rating,
            images = d.images,
        )
    }
}
