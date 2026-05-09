package com.voyager.tourism.data.dto

import com.squareup.moshi.JsonClass

/**
 * Body for [POST auth/google/token] — native `serverAuthCode` (Android) intercambiado en backend-core.
 */
@JsonClass(generateAdapter = true)
data class GoogleServerAuthRequest(
    val code: String,
)
