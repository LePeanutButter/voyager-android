package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Respuesta de actividad en plan (`TravelPlanActivityDto` del backend).
 */
@JsonClass(generateAdapter = true)
data class TravelPlanActivityDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "startTime") val startTime: String? = null,
    @Json(name = "endTime") val endTime: String? = null,
    @Json(name = "location") val location: String? = null,
    @Json(name = "estimatedCost") val estimatedCost: Double? = null,
    @Json(name = "actualCost") val actualCost: Double? = null,
    @Json(name = "bookingReference") val bookingReference: String? = null,
    @Json(name = "isConfirmed") val isConfirmed: Boolean? = null,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "createdAt") val createdAt: String? = null,
    @Json(name = "updatedAt") val updatedAt: String? = null,
)

@JsonClass(generateAdapter = true)
data class UpdateActivityRequest(
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "startTime") val startTime: String,
    @Json(name = "endTime") val endTime: String,
    @Json(name = "location") val location: String? = null,
)

@JsonClass(generateAdapter = true)
data class CreateActivityRequest(
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "startTime") val startTime: String,
    @Json(name = "endTime") val endTime: String,
    @Json(name = "location") val location: String? = null,
)
