package com.voyager.tourism.data.api

import com.voyager.tourism.data.dto.ApiResponse
import com.voyager.tourism.data.dto.GoogleServerAuthRequest
import com.voyager.tourism.data.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Google Authentication API service interface
 * Matches backend [com.tourism.platform.controller.GoogleAuthController] routes.
 */
interface GoogleAuthApiService {

    /**
     * Initiate Google OAuth2 login
     * GET /auth/google/login
     * Returns a 302 redirect. We use Response<Unit> to capture headers without parsing body.
     */
    @GET("auth/google/login")
    suspend fun initiateGoogleLogin(): retrofit2.Response<Unit>

    /**
     * Intercambia un `serverAuthCode` (Android Play Services) o código equivalente por sesión Voyager (JSON).
     * POST /auth/google/token
     */
    @POST("auth/google/token")
    suspend fun exchangeGoogleServerAuthCode(@Body body: GoogleServerAuthRequest): ApiResponse<UserDto>
}
