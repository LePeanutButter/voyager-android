package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Estados de plan de viaje alineados con `com.tourism.platform.model.TravelPlanStatus`.
 */
@JsonClass(generateAdapter = true)
enum class TravelPlanStatus {
    @Json(name = "DRAFT")
    DRAFT,

    @Json(name = "ACTIVE")
    ACTIVE,

    @Json(name = "COMPLETED")
    COMPLETED,

    @Json(name = "CANCELLED")
    CANCELLED,

    @Json(name = "ON_HOLD")
    ON_HOLD,

    @Json(name = "ARCHIVED")
    ARCHIVED,
}
