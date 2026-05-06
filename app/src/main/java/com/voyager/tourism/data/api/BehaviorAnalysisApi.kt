package com.voyager.tourism.data.api

import com.voyager.tourism.data.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Behavior Analysis API service interface
 * Handles implicit user behavior tracking and analysis endpoints
 */
interface BehaviorAnalysisApi {
    
    @POST("api/v1/behavior-analysis/track")
    suspend fun trackUserBehavior(
        @Body request: BehaviorTrackingRequest,
        @Header("Authorization") token: String
    ): Response<ApiResponse>
    
    @POST("api/v1/behavior-analysis/analyze")
    suspend fun analyzeUserBehavior(
        @Body request: BehaviorAnalysisRequest,
        @Header("Authorization") token: String
    ): Response<ImplicitPreferenceUpdate>
    
    @GET("api/v1/behavior-analysis/summary/{userId}")
    suspend fun getBehaviorSummary(
        @Path("userId") userId: String,
        @Query("days") days: Int = 30,
        @Header("Authorization") token: String
    ): Response<BehaviorSummary>
    
    @POST("api/v1/behavior-analysis/batch-track")
    suspend fun batchTrackBehavior(
        @Body requests: List<BehaviorTrackingRequest>,
        @Header("Authorization") token: String
    ): Response<ApiResponse>
    
    @GET("api/v1/behavior-analysis/patterns/{userId}")
    suspend fun getDetectedPatterns(
        @Path("userId") userId: String,
        @Query("days") days: Int = 7,
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>
    
    @DELETE("api/v1/behavior-analysis/clear/{userId}")
    suspend fun clearUserBehaviorData(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<ApiResponse>
}
