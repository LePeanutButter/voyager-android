package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object for travel plan responses
 * Matches exactly the backend TravelPlanDto structure
 */
@JsonClass(generateAdapter = true)
data class TravelPlanDto(
    @Json(name = "id")
    val id: Long, // Backend uses Long
    
    @Json(name = "title")
    val title: String,
    
    @Json(name = "description")
    val description: String? = null,
    
    @Json(name = "destination")
    val destination: String,
    
    @Json(name = "startDate")
    val startDate: String, // LocalDateTime from backend
    
    @Json(name = "endDate")
    val endDate: String, // LocalDateTime from backend
    
    @Json(name = "budget")
    val budget: Double? = null,
    
    @Json(name = "status")
    val status: TravelPlanStatus,
    
    @Json(name = "travelType")
    val travelType: TravelType,
    
    @Json(name = "userId")
    val userId: Long,
    
    @Json(name = "createdAt")
    val createdAt: String? = null, // LocalDateTime from backend
    
    @Json(name = "updatedAt")
    val updatedAt: String? = null // LocalDateTime from backend
)
