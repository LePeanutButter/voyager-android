package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Enum matching backend UserRole
 */
@JsonClass(generateAdapter = true)
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
