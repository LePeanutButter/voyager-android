package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Misma forma que persiste el web en `localStorage` bajo `smartrip_settings`
 * ([SettingsPage.jsx] en voyager-web-client).
 */
@JsonClass(generateAdapter = true)
data class SmarTripSettingsPayload(
    @Json(name = "darkMode")
    val darkMode: Boolean = false,
    @Json(name = "communitySuggestions")
    val communitySuggestions: Boolean = true,
    @Json(name = "profileVisibility")
    val profileVisibility: String = "community",
)
