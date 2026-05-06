package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Enum matching backend TravelPlanStatus
 */
@JsonClass(generateAdapter = true)
enum class TravelPlanStatus(val value: String) {
    @Json(name = "DRAFT")
    DRAFT("DRAFT"),
    
    @Json(name = "PLANNED")
    PLANNED("PLANNED"),
    
    @Json(name = "IN_PROGRESS")
    IN_PROGRESS("IN_PROGRESS"),
    
    @Json(name = "COMPLETED")
    COMPLETED("COMPLETED"),
    
    @Json(name = "CANCELLED")
    CANCELLED("CANCELLED");
    
    companion object {
        fun fromValue(value: String): TravelPlanStatus {
            return values().find { it.value == value } ?: DRAFT
        }
    }
}
