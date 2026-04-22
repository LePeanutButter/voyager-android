package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object for User API responses
 * Used for mapping between API responses and domain models
 */
@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "id")
    val id: String,
    
    @Json(name = "email")
    val email: String,
    
    @Json(name = "username")
    val username: String,
    
    @Json(name = "first_name")
    val firstName: String,
    
    @Json(name = "last_name")
    val lastName: String,
    
    @Json(name = "avatar")
    val avatar: String? = null,
    
    @Json(name = "preferences")
    val preferences: UserPreferencesDto? = null,
    
    @Json(name = "is_verified")
    val isVerified: Boolean = false,
    
    @Json(name = "created_at")
    val createdAt: Long,
    
    @Json(name = "updated_at")
    val updatedAt: Long
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

/**
 * Login request DTO
 */
@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "email")
    val email: String,
    
    @Json(name = "password")
    val password: String
)

/**
 * Login response DTO
 */
@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "user")
    val user: UserDto,
    
    @Json(name = "token")
    val token: String,
    
    @Json(name = "refresh_token")
    val refreshToken: String,
    
    @Json(name = "expires_in")
    val expiresIn: Long
)

/**
 * Register request DTO
 */
@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "email")
    val email: String,
    
    @Json(name = "password")
    val password: String,
    
    @Json(name = "username")
    val username: String,
    
    @Json(name = "first_name")
    val firstName: String,
    
    @Json(name = "last_name")
    val lastName: String
)
