package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Request for Google OAuth2 mobile callback
 */
@JsonClass(generateAdapter = true)
data class GoogleCallbackRequest(
    @Json(name = "code")
    val code: String,
    
    @Json(name = "state")
    val state: String? = null
)
