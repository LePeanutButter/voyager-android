package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object for user profile update requests
 * Contains fields for updating user profile information
 */
@JsonClass(generateAdapter = true)
data class UserUpdateRequest(
    @Json(name = "first_name")
    val firstName: String,
    
    @Json(name = "last_name")
    val lastName: String,
    
    @Json(name = "bio")
    val bio: String,
    
    @Json(name = "phone_number")
    val phoneNumber: String? = null,
    
    @Json(name = "interests")
    val interests: List<String> = emptyList()
)
