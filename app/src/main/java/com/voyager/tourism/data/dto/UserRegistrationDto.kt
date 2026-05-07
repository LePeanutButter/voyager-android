package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object for user registration requests
 * Matches exactly the backend UserRegistrationDto structure
 */
@JsonClass(generateAdapter = true)
data class UserRegistrationDto(
    @Json(name = "username")
    val username: String,
    
    @Json(name = "email")
    val email: String,
    
    @Json(name = "password")
    val password: String,
    
    @Json(name = "firstName")
    val firstName: String,
    
    @Json(name = "lastName")
    val lastName: String,
    
    @Json(name = "phoneNumber")
    val phoneNumber: String? = null
)
