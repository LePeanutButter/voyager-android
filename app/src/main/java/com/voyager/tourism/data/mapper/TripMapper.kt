package com.voyager.tourism.data.mapper

import com.voyager.tourism.data.database.entity.TripEntity
import com.voyager.tourism.data.dto.TripDto
import com.voyager.tourism.domain.model.Trip
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mapper for converting between different Trip representations
 * Handles conversion between domain model, DTO, and entity
 */
@Singleton
class TripMapper @Inject constructor() {
    
    fun toDomain(tripDto: TripDto): Trip {
        return Trip(
            id = tripDto.id,
            userId = tripDto.userId,
            title = tripDto.title,
            description = tripDto.description,
            destination = tripDto.destination,
            startDate = tripDto.startDate,
            endDate = tripDto.endDate,
            budget = tripDto.budget,
            status = tripDto.status,
            createdAt = tripDto.createdAt,
            updatedAt = tripDto.updatedAt
        )
    }
    
    fun toDomain(tripEntity: TripEntity): Trip {
        return Trip(
            id = tripEntity.id,
            userId = tripEntity.userId,
            title = tripEntity.title,
            description = tripEntity.description,
            destination = tripEntity.destination,
            startDate = tripEntity.startDate,
            endDate = tripEntity.endDate,
            budget = tripEntity.budget,
            status = tripEntity.status,
            createdAt = tripEntity.createdAt,
            updatedAt = tripEntity.updatedAt
        )
    }
    
    fun toDto(trip: Trip): TripDto {
        return TripDto(
            id = trip.id,
            userId = trip.userId,
            title = trip.title,
            description = trip.description,
            destination = trip.destination,
            startDate = trip.startDate,
            endDate = trip.endDate,
            budget = trip.budget,
            status = trip.status,
            createdAt = trip.createdAt,
            updatedAt = trip.updatedAt
        )
    }
    
    fun toEntity(tripDto: TripDto): TripEntity {
        return TripEntity(
            id = tripDto.id,
            userId = tripDto.userId,
            title = tripDto.title,
            description = tripDto.description,
            destination = tripDto.destination,
            startDate = tripDto.startDate,
            endDate = tripDto.endDate,
            budget = tripDto.budget,
            status = tripDto.status,
            createdAt = tripDto.createdAt,
            updatedAt = tripDto.updatedAt
        )
    }
    
    fun toEntity(trip: Trip): TripEntity {
        return TripEntity(
            id = trip.id,
            userId = trip.userId,
            title = trip.title,
            description = trip.description,
            destination = trip.destination,
            startDate = trip.startDate,
            endDate = trip.endDate,
            budget = trip.budget,
            status = trip.status,
            createdAt = trip.createdAt,
            updatedAt = trip.updatedAt
        )
    }
}
