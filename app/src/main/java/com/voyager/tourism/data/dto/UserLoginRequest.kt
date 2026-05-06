package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object for user login requests
 * Contains credentials for user authentication
 */
@JsonClass(generateAdapter = true)
data class UserLoginRequest(
    @Json(name = "usernameOrEmail")
    val usernameOrEmail: String,
    
    @Json(name = "password")
    val password: String
)
