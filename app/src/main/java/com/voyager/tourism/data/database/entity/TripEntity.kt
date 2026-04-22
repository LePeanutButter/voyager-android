package com.voyager.tourism.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Room database entity for Trip table
 * Represents local storage of trip data
 */
@Entity(
    tableName = "trips",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class TripEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val destination: String, // JSON string of Destination
    val startDate: Long,
    val endDate: Long,
    val budget: Double,
    val travelers: Int,
    val status: String,
    val itinerary: String? = null, // JSON string of List<ItineraryItem>
    val accommodations: String? = null, // JSON string of List<Accommodation>
    val activities: String? = null, // JSON string of List<Activity>
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * Room database entity for Destination table
 */
@Entity(tableName = "destinations")
data class DestinationEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val country: String,
    val coordinates: String, // JSON string of Coordinates
    val timezone: String,
    val currency: String,
    val language: String,
    val climate: String,
    val bestTimeToVisit: String,
    val averageCost: Double,
    val rating: Float,
    val images: String? = null // JSON string of List<String>
)

/**
 * Room database entity for Itinerary items
 */
@Entity(
    tableName = "itinerary_items",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["tripId"])]
)
data class ItineraryItemEntity(
    @PrimaryKey
    val id: String,
    val tripId: String,
    val day: Int,
    val startTime: Long,
    val endTime: Long,
    val title: String,
    val description: String,
    val location: String,
    val type: String,
    val cost: Double,
    val isBooked: Boolean = false
)
