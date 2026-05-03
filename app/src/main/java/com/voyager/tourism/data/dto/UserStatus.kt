package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Enum matching backend UserStatus
 */
@JsonClass(generateAdapter = true)
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
        fun fromValue(value: String): UserStatus {
            return values().find { it.value == value } ?: ACTIVE
        }
    }
}
