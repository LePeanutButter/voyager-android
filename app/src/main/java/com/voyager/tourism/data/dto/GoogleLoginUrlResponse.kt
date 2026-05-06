package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response containing Google OAuth2 login URL
 */
@JsonClass(generateAdapter = true)
data class GoogleLoginUrlResponse(
    @Json(name = "loginUrl")
    val loginUrl: String,
    
    @Json(name = "state")
    val state: String
)
