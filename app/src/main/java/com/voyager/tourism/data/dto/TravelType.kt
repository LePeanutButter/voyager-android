package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Enum matching backend TravelType
 */
@JsonClass(generateAdapter = true)
enum class TravelType(val value: String) {
    @Json(name = "LEISURE")
    LEISURE("LEISURE"),
    
    @Json(name = "BUSINESS")
    BUSINESS("BUSINESS"),
    
    @Json(name = "ADVENTURE")
    ADVENTURE("ADVENTURE"),
    
    @Json(name = "CULTURAL")
    CULTURAL("CULTURAL"),
    
    @Json(name = "ECO_TOURISM")
    ECO_TOURISM("ECO_TOURISM"),
    
    @Json(name = "MEDICAL")
    MEDICAL("MEDICAL"),
    
    @Json(name = "EDUCATIONAL")
    EDUCATIONAL("EDUCATIONAL"),
    
    @Json(name = "RELIGIOUS")
    RELIGIOUS("RELIGIOUS"),
    
    @Json(name = "SPORTS")
    SPORTS("SPORTS"),
    
    @Json(name = "OTHER")
    OTHER("OTHER");
    
    companion object {
        fun fromValue(value: String): TravelType {
            return values().find { it.value == value } ?: LEISURE
        }
    }
}
