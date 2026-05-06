package com.voyager.tourism.data.dto

import com.squareup.moshi.Json

/**
 * Enum matching backend UserRole
 */
enum class UserRole(val value: String) {
    @Json(name = "USER")
    USER("USER"),
    
    @Json(name = "ADMIN")
    ADMIN("ADMIN"),
    
    @Json(name = "SUPER_ADMIN")
    SUPER_ADMIN("SUPER_ADMIN");
    
    companion object {
        /**
         * Returns the [UserRole] whose serialized [value] matches [value], or [USER] if none match.
         *
         * @param value Backend string value (e.g. `"ADMIN"`).
         */
        fun fromValue(value: String): UserRole {
            return values().find { it.value == value } ?: USER
        }
    }
}
