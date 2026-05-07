package com.voyager.tourism.data.dto

import com.squareup.moshi.Json

/**
 * Enum matching backend UserStatus
 */
enum class UserStatus(val value: String) {
    @Json(name = "ACTIVE")
    ACTIVE("ACTIVE"),
    
    @Json(name = "INACTIVE")
    INACTIVE("INACTIVE"),
    
    @Json(name = "SUSPENDED")
    SUSPENDED("SUSPENDED"),
    
    @Json(name = "PENDING")
    PENDING("PENDING");
    
    companion object {
        /**
         * Returns the [UserStatus] whose serialized [value] matches [value], or [ACTIVE] if none match.
         *
         * @param value Backend string value (e.g. `"PENDING"`).
         */
        fun fromValue(value: String): UserStatus {
            return values().find { it.value == value } ?: ACTIVE
        }
    }
}
