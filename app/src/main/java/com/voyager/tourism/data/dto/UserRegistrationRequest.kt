package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object for user registration requests
 * Contains all required fields for creating a new user account
 */
@JsonClass(generateAdapter = true)
data class UserRegistrationRequest(
    @Json(name = "username")
    val username: String,
    
    @Json(name = "email")
    val email: String,
    
    @Json(name = "password")
    val password: String,
    
    @Json(name = "first_name")
    val firstName: String,
    
    @Json(name = "last_name")
    val lastName: String,
    
    @Json(name = "phone_number")
    val phoneNumber: String? = null
)
