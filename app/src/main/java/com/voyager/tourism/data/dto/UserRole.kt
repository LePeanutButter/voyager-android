package com.voyager.tourism.data.dto

import com.squareup.moshi.Json

/**
 * Valores serializados igual que [com.tourism.platform.model.UserRole] en voyager-backend-core.
 */
enum class UserRole(val value: String) {
    @Json(name = "TRAVELER")
    TRAVELER("TRAVELER"),

    @Json(name = "SERVICE_PROVIDER")
    SERVICE_PROVIDER("SERVICE_PROVIDER"),

    @Json(name = "GUIDE")
    GUIDE("GUIDE"),

    @Json(name = "ADMIN")
    ADMIN("ADMIN"),

    @Json(name = "SUPER_ADMIN")
    SUPER_ADMIN("SUPER_ADMIN");

    companion object {
        /**
         * Resuelve el rol a partir del string del backend o dominio almacenado.
         * `"USER"` se trata como alias legado de [TRAVELER].
         */
        fun fromValue(value: String): UserRole {
            if (value == "USER") return TRAVELER
            return UserRole.entries.find { it.value == value } ?: TRAVELER
        }
    }
}
