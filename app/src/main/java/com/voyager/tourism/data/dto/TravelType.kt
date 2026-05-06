package com.voyager.tourism.data.dto

import com.squareup.moshi.Json

/**
 * Enum matching backend TravelType
 */
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
        /**
         * Returns the [TravelType] whose serialized [value] matches [value], or [LEISURE] if none match.
         *
         * @param value Backend string value (e.g. `"LEISURE"`).
         */
        fun fromValue(value: String): TravelType {
            return values().find { it.value == value } ?: LEISURE
        }
    }
}
