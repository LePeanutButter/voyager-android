package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object for User API responses
 * Matches exactly the backend UserDto structure
 */
@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "id")
    val id: Long, // Backend uses Long
    
    @Json(name = "username")
    val username: String,
    
    @Json(name = "email")
    val email: String,
    
    @Json(name = "firstName")
    val firstName: String,
    
    @Json(name = "lastName")
    val lastName: String,
    
    @Json(name = "phoneNumber")
    val phoneNumber: String? = null,
    
    @Json(name = "role")
    val role: UserRole,
    
    @Json(name = "status")
    val status: UserStatus,
    
    @Json(name = "profileImageUrl")
    val profileImageUrl: String? = null,
    
    @Json(name = "bio")
    val bio: String? = null,
    
    @Json(name = "interests")
    val interests: Set<String>? = null,
    
    @Json(name = "dateOfBirth")
    val dateOfBirth: String? = null, // LocalDateTime from backend
    
    @Json(name = "createdAt")
    val createdAt: String? = null, // LocalDateTime from backend
    
    @Json(name = "updatedAt")
    val updatedAt: String? = null, // LocalDateTime from backend
    
    @Json(name = "token")
    val token: String? = null
)

/**
 * User preferences DTO
 */
@JsonClass(generateAdapter = true)
data class UserPreferencesDto(
    @Json(name = "preferred_destinations")
    val preferredDestinations: List<String> = emptyList(),
    
    @Json(name = "travel_style")
    val travelStyle: String = "BALANCED",
    
    @Json(name = "budget_range")
    val budgetRange: String = "MEDIUM",
    
    @Json(name = "interests")
    val interests: List<String> = emptyList(),
    
    @Json(name = "accommodation_type")
    val accommodationType: String = "HOTEL",
    
    @Json(name = "notifications_enabled")
    val notificationsEnabled: Boolean = true
)
