package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object for user login requests
 * Matches exactly the backend UserLoginDto structure
 */
@JsonClass(generateAdapter = true)
data class UserLoginDto(
    @Json(name = "usernameOrEmail")
    val usernameOrEmail: String,
    
    @Json(name = "password")
    val password: String
)
