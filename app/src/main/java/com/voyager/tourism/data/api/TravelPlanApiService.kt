package com.voyager.tourism.data.api

import com.voyager.tourism.data.dto.ApiResponse
import com.voyager.tourism.data.dto.TravelPlanDto
import retrofit2.http.*

/**
 * Travel Plan API service interface
 * Matches exactly the backend TravelPlanController endpoints
 */
interface TravelPlanApiService {
    
    /**
     * Create a new travel plan
     * POST /travel-plans
     */
    @POST("travel-plans")
    suspend fun createTravelPlan(@Body request: TravelPlanDto): ApiResponse<TravelPlanDto>
    
    /**
     * Get all travel plans
     * GET /travel-plans
     */
    @GET("travel-plans")
    suspend fun getTravelPlans(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sortBy") sortBy: String = "createdAt",
        @Query("sortDir") sortDir: String = "desc"
    ): ApiResponse<List<TravelPlanDto>>
    
    /**
     * Get travel plan by ID
     * GET /travel-plans/{id}
     */
    @GET("travel-plans/{id}")
    suspend fun getTravelPlanById(@Path("id") id: Long): ApiResponse<TravelPlanDto>
    
    /**
     * Update travel plan
     * PUT /travel-plans/{id}
     */
    @PUT("travel-plans/{id}")
    suspend fun updateTravelPlan(@Path("id") id: Long, @Body request: TravelPlanDto): ApiResponse<TravelPlanDto>
    
    /**
     * Delete travel plan
     * DELETE /travel-plans/{id}
     */
    @DELETE("travel-plans/{id}")
    suspend fun deleteTravelPlan(@Path("id") id: Long): ApiResponse<Unit>
    
    /**
     * Get travel plans by user
     * GET /travel-plans?userId={userId}
     */
    @GET("travel-plans")
    suspend fun getTravelPlansByUser(
        @Query("userId") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<List<TravelPlanDto>>
}
