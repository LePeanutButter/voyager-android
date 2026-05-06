package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDateTime

/**
 * DTO for representing traveler compatibility matches
 * 
 * This DTO contains information about travelers who have similar
 * destinations and compatible travel dates for social matching.
 */
@JsonClass(generateAdapter = true)
data class TravelerMatchDto(
    @Json(name = "userId")
    val userId: Long,
    
    @Json(name = "username")
    val username: String,
    
    @Json(name = "firstName")
    val firstName: String,
    
    @Json(name = "lastName")
    val lastName: String,
    
    @Json(name = "profileImageUrl")
    val profileImageUrl: String? = null,
    
    @Json(name = "bio")
    val bio: String? = null,
    
    @Json(name = "travelPlanId")
    val travelPlanId: Long,
    
    @Json(name = "travelPlanTitle")
    val travelPlanTitle: String,
    
    @Json(name = "destinationLocation")
    val destinationLocation: String,
    
    @Json(name = "travelStartDate")
    val travelStartDate: String, // Using String for JSON compatibility
    
    @Json(name = "travelEndDate")
    val travelEndDate: String, // Using String for JSON compatibility
    
    @Json(name = "numberOfTravelers")
    val numberOfTravelers: Int,
    
    @Json(name = "daysOverlap")
    val daysOverlap: Int,
    
    @Json(name = "compatibilityScore")
    val compatibilityScore: Double
)
