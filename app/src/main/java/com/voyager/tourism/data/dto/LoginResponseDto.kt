package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Respuesta de [POST /users/login], alineada con
 * [com.tourism.platform.dto.LoginResponseDto] y con el cliente web (`authService.login`).
 */
@JsonClass(generateAdapter = true)
data class LoginResponseDto(
    @Json(name = "token")
    val token: String,
    @Json(name = "tokenType")
    val tokenType: String? = "Bearer",
    @Json(name = "expiresIn")
    val expiresIn: Long? = null,
    @Json(name = "user")
    val user: UserDto,
)
