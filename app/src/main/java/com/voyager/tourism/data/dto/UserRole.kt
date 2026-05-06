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
        fun fromValue(value: String): UserRole {
            return values().find { it.value == value } ?: USER
        }
    }
}
