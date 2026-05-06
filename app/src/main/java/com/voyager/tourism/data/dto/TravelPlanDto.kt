package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Travel plan aggregate returned by the platform API, aligned with backend `TravelPlanDto` (Jackson camelCase).
 */
@JsonClass(generateAdapter = true)
data class TravelPlanDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "status") val status: TravelPlanStatus? = null,
    @Json(name = "travelType") val travelType: TravelType? = null,
    @Json(name = "startDate") val startDate: String? = null,
    @Json(name = "endDate") val endDate: String? = null,
    @Json(name = "estimatedBudget") val estimatedBudget: Double? = null,
    @Json(name = "actualCost") val actualCost: Double? = null,
    @Json(name = "numberOfTravelers") val numberOfTravelers: Int? = null,
    @Json(name = "originLocation") val originLocation: String? = null,
    @Json(name = "destinationLocation") val destinationLocation: String? = null,
    @Json(name = "isPublic") val isPublic: Boolean? = null,
    @Json(name = "shareToken") val shareToken: String? = null,
    @Json(name = "activities") val activities: List<TravelPlanActivityDto>? = null,
    @Json(name = "reservations") val reservations: List<ReservationDto>? = null,
    @Json(name = "createdAt") val createdAt: String? = null,
    @Json(name = "updatedAt") val updatedAt: String? = null,
)
