package com.voyager.tourism.data.api

import com.voyager.tourism.data.dto.ApiResponse
import com.voyager.tourism.data.dto.UserDto
import com.voyager.tourism.data.dto.UserUpdateRequest
import retrofit2.http.*

/**
 * User API service interface
 * Handles user profile management operations
 */
interface UserApiService {
    
    /**
     * Get user by ID
     */
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: String): ApiResponse<UserDto>
    
    /**
     * Update user profile
     */
    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body request: UserUpdateRequest
    ): ApiResponse<UserDto>
}
