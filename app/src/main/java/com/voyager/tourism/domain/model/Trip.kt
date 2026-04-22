package com.voyager.tourism.domain.model

/**
 * Domain model representing a travel trip
 */
data class Trip(
    val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val destination: Destination,
    val startDate: Long,
    val endDate: Long,
    val budget: Double,
    val travelers: Int,
    val status: TripStatus,
    val itinerary: List<ItineraryItem> = emptyList(),
    val accommodations: List<Accommodation> = emptyList(),
    val activities: List<Activity> = emptyList(),
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * Destination information
 */
data class Destination(
    val id: String,
    val name: String,
    val country: String,
    val coordinates: Coordinates,
    val timezone: String,
    val currency: String,
    val language: String,
    val climate: String,
    val bestTimeToVisit: String,
    val averageCost: Double,
    val rating: Float,
    val images: List<String> = emptyList()
)

/**
 * Geographic coordinates
 */
data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

/**
 * Trip status enumeration
 */
enum class TripStatus {
    PLANNING, CONFIRMED, ACTIVE, COMPLETED, CANCELLED
}

/**
 * Individual itinerary items
 */
data class ItineraryItem(
    val id: String,
    val day: Int,
    val startTime: Long,
    val endTime: Long,
    val title: String,
    val description: String,
    val location: String,
    val type: ItineraryType,
    val cost: Double,
    val isBooked: Boolean = false
)

enum class ItineraryType {
    FLIGHT, ACCOMMODATION, ACTIVITY, MEAL, TRANSPORTATION, SIGHTSEEING
}

/**
 * Accommodation details
 */
data class Accommodation(
    val id: String,
    val name: String,
    val type: AccommodationType,
    val address: String,
    val rating: Float,
    val pricePerNight: Double,
    val checkIn: Long,
    val checkOut: Long,
    val amenities: List<String>,
    val images: List<String> = emptyList()
)

/**
 * Activity details
 */
data class Activity(
    val id: String,
    val name: String,
    val description: String,
    val location: String,
    val duration: Int, // in minutes
    val price: Double,
    val rating: Float,
    val category: ActivityCategory,
    val images: List<String> = emptyList()
)

enum class ActivityCategory {
    TOUR, ADVENTURE, CULTURAL, ENTERTAINMENT, SPORTS, RELAXATION, FOOD
}
