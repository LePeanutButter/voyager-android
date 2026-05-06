package com.voyager.tourism.data.api

import com.voyager.tourism.data.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Behavior Analysis API service interface
 * Handles implicit user behavior tracking and analysis endpoints
 */
interface BehaviorAnalysisApi {
    
    @POST("behavior-analysis/track")
    suspend fun trackUserBehavior(
        @Body request: BehaviorTrackingRequest,
        @Header("Authorization") token: String
    ): Response<BehaviorAnalysisSimpleResponse>
    
    @POST("behavior-analysis/analyze")
    suspend fun analyzeUserBehavior(
        @Body request: BehaviorAnalysisRequest,
        @Header("Authorization") token: String
    ): Response<ImplicitPreferenceUpdate>
    
    @GET("behavior-analysis/summary/{userId}")
    suspend fun getBehaviorSummary(
        @Path("userId") userId: String,
        @Query("days") days: Int = 30,
        @Header("Authorization") token: String
    ): Response<BehaviorSummary>
    
    @POST("behavior-analysis/batch-track")
    suspend fun batchTrackBehavior(
        @Body requests: List<BehaviorTrackingRequest>,
        @Header("Authorization") token: String
    ): Response<BehaviorAnalysisSimpleResponse>
    
    @GET("behavior-analysis/patterns/{userId}")
    suspend fun getDetectedPatterns(
        @Path("userId") userId: String,
        @Query("days") days: Int = 7,
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>
    
    @DELETE("behavior-analysis/clear/{userId}")
    suspend fun clearUserBehaviorData(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<BehaviorAnalysisSimpleResponse>
}
