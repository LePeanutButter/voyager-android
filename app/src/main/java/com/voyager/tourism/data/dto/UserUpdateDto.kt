package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object for user update requests
 * Matches exactly the backend UserUpdateDto structure
 */
@JsonClass(generateAdapter = true)
data class UserUpdateDto(
    @Json(name = "firstName")
    val firstName: String? = null,
    
    @Json(name = "lastName")
    val lastName: String? = null,
    
    @Json(name = "phoneNumber")
    val phoneNumber: String? = null,
    
    @Json(name = "profileImageUrl")
    val profileImageUrl: String? = null,
    
    @Json(name = "bio")
    val bio: String? = null,
    
    @Json(name = "interests")
    val interests: List<String>? = null,
    
    @Json(name = "dateOfBirth")
    val dateOfBirth: String? = null // LocalDateTime from backend
)
