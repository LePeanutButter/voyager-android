package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object for Trip API responses
 */
@JsonClass(generateAdapter = true)
data class TripDto(
    @Json(name = "id")
    val id: String,
    
    @Json(name = "user_id")
    val userId: String,
    
    @Json(name = "title")
    val title: String,
    
    @Json(name = "description")
    val description: String,
    
    @Json(name = "destination")
    val destination: DestinationDto,
    
    @Json(name = "start_date")
    val startDate: Long,
    
    @Json(name = "end_date")
    val endDate: Long,
    
    @Json(name = "budget")
    val budget: Double,
    
    @Json(name = "travelers")
    val travelers: Int,
    
    @Json(name = "status")
    val status: String,
    
    @Json(name = "itinerary")
    val itinerary: List<ItineraryItemDto> = emptyList(),
    
    @Json(name = "accommodations")
    val accommodations: List<AccommodationDto> = emptyList(),
    
    @Json(name = "activities")
    val activities: List<ActivityDto> = emptyList(),
    
    @Json(name = "created_at")
    val createdAt: Long,
    
    @Json(name = "updated_at")
    val updatedAt: Long
)

/**
 * Destination DTO
 */
@JsonClass(generateAdapter = true)
data class DestinationDto(
    @Json(name = "id")
    val id: String,
    
    @Json(name = "name")
    val name: String,
    
    @Json(name = "country")
    val country: String,
    
    @Json(name = "coordinates")
    val coordinates: CoordinatesDto,
    
    @Json(name = "timezone")
    val timezone: String,
    
    @Json(name = "currency")
    val currency: String,
    
    @Json(name = "language")
    val language: String,
    
    @Json(name = "climate")
    val climate: String,
    
    @Json(name = "best_time_to_visit")
    val bestTimeToVisit: String,
    
    @Json(name = "average_cost")
    val averageCost: Double,
    
    @Json(name = "rating")
    val rating: Float,
    
    @Json(name = "images")
    val images: List<String> = emptyList()
)

/**
 * Coordinates DTO
 */
@JsonClass(generateAdapter = true)
data class CoordinatesDto(
    @Json(name = "latitude")
    val latitude: Double,
    
    @Json(name = "longitude")
    val longitude: Double
)

/**
 * Itinerary item DTO
 */
@JsonClass(generateAdapter = true)
data class ItineraryItemDto(
    @Json(name = "id")
    val id: String,
    
    @Json(name = "day")
    val day: Int,
    
    @Json(name = "start_time")
    val startTime: Long,
    
    @Json(name = "end_time")
    val endTime: Long,
    
    @Json(name = "title")
    val title: String,
    
    @Json(name = "description")
    val description: String,
    
    @Json(name = "location")
    val location: String,
    
    @Json(name = "type")
    val type: String,
    
    @Json(name = "cost")
    val cost: Double,
    
    @Json(name = "is_booked")
    val isBooked: Boolean = false
)

/**
 * Accommodation DTO
 */
@JsonClass(generateAdapter = true)
data class AccommodationDto(
    @Json(name = "id")
    val id: String,
    
    @Json(name = "name")
    val name: String,
    
    @Json(name = "type")
    val type: String,
    
    @Json(name = "address")
    val address: String,
    
    @Json(name = "rating")
    val rating: Float,
    
    @Json(name = "price_per_night")
    val pricePerNight: Double,
    
    @Json(name = "check_in")
    val checkIn: Long,
    
    @Json(name = "check_out")
    val checkOut: Long,
    
    @Json(name = "amenities")
    val amenities: List<String>,
    
    @Json(name = "images")
    val images: List<String> = emptyList()
)

/**
 * Activity DTO
 */
@JsonClass(generateAdapter = true)
data class ActivityDto(
    @Json(name = "id")
    val id: String,
    
    @Json(name = "name")
    val name: String,
    
    @Json(name = "description")
    val description: String,
    
    @Json(name = "location")
    val location: String,
    
    @Json(name = "duration")
    val duration: Int,
    
    @Json(name = "price")
    val price: Double,
    
    @Json(name = "rating")
    val rating: Float,
    
    @Json(name = "category")
    val category: String,
    
    @Json(name = "images")
    val images: List<String> = emptyList()
)

/**
 * AI Chat request DTO
 */
@JsonClass(generateAdapter = true)
data class AiChatRequest(
    @Json(name = "message")
    val message: String,
    
    @Json(name = "context")
    val context: String? = null
)

/**
 * AI Chat response DTO
 */
@JsonClass(generateAdapter = true)
data class AiChatResponse(
    @Json(name = "response")
    val response: String,
    
    @Json(name = "suggestions")
    val suggestions: List<String> = emptyList(),
    
    @Json(name = "timestamp")
    val timestamp: Long
)
