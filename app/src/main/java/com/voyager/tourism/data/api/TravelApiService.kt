package com.voyager.tourism.data.api

import com.voyager.tourism.data.dto.ApiResponse
import com.voyager.tourism.data.dto.TravelPlanDto
import com.voyager.tourism.data.dto.TravelPlanRequest
import retrofit2.http.*

/**
 * Travel API service interface
 * Handles travel plan management operations
 */
interface TravelApiService {
    
    /**
     * Create a new travel plan
     */
    @POST("travel-plans")
    suspend fun createTravelPlan(@Body request: TravelPlanRequest): ApiResponse<TravelPlanDto>
    
    /**
     * Get travel plans for a user
     */
    @GET("travel-plans")
    suspend fun getUserTravelPlans(@Query("user_id") userId: String): ApiResponse<List<TravelPlanDto>>
    
    /**
     * Get travel plan by ID
     */
    @GET("travel-plans/{id}")
    suspend fun getTravelPlanById(@Path("id") id: String): ApiResponse<TravelPlanDto>
    
    /**
     * Update travel plan
     */
    @PUT("travel-plans/{id}")
    suspend fun updateTravelPlan(
        @Path("id") id: String,
        @Body request: TravelPlanRequest
    ): ApiResponse<TravelPlanDto>
    
    /**
     * Delete travel plan
     */
    @DELETE("travel-plans/{id}")
    suspend fun deleteTravelPlan(@Path("id") id: String): ApiResponse<Unit>
}
