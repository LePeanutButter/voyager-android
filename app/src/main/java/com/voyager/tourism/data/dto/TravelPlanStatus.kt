package com.voyager.tourism.data.dto

import com.squareup.moshi.Json

/**
 * Estados de plan de viaje alineados con `com.tourism.platform.model.TravelPlanStatus`.
 */
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
