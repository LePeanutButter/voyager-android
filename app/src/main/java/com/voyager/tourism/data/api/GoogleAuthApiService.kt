package com.voyager.tourism.data.api

import com.voyager.tourism.data.dto.ApiResponse
import com.voyager.tourism.data.dto.UserDto
import retrofit2.http.*

/**
 * Google Authentication API service interface
 * Matches exactly with backend GoogleAuthController endpoints
 */
interface GoogleAuthApiService {
    
    /**
     * Initiate Google OAuth2 login
     * GET /auth/google/login
     */
    @GET("auth/google/login")
    suspend fun initiateGoogleLogin(): ApiResponse<String>
    
    /**
     * Handle Google OAuth2 callback
     * GET /auth/google/callback?code={code}&state={state}
     */
    @GET("auth/google/callback")
    suspend fun handleGoogleCallback(
        @Query("code") code: String,
        @Query("state") state: String
    ): ApiResponse<UserDto>
}
