package com.voyager.tourism.data.api

import com.voyager.tourism.data.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Remote API for implicit user behavior tracking and preference inference.
 */
interface BehaviorAnalysisApi {
    
    /**
     * Records a single tracked interaction for the authenticated user.
     */
    @POST("behavior-analysis/track")
    suspend fun trackUserBehavior(
        @Body request: BehaviorTrackingRequest,
        @Header("Authorization") token: String
    ): Response<BehaviorAnalysisSimpleResponse>
    
    /**
     * Runs an on-demand behavior analysis and returns inferred preference updates.
     */
    @POST("behavior-analysis/analyze")
    suspend fun analyzeUserBehavior(
        @Body request: BehaviorAnalysisRequest,
        @Header("Authorization") token: String
    ): Response<ImplicitPreferenceUpdate>
    
    /**
     * Returns aggregated behavior metrics for a user across the last [days] days.
     */
    @GET("behavior-analysis/summary/{userId}")
    suspend fun getBehaviorSummary(
        @Path("userId") userId: String,
        @Query("days") days: Int = 30,
        @Header("Authorization") token: String
    ): Response<BehaviorSummary>
    
    /**
     * Uploads multiple tracking events in a single batch request.
     */
    @POST("behavior-analysis/batch-track")
    suspend fun batchTrackBehavior(
        @Body requests: List<BehaviorTrackingRequest>,
        @Header("Authorization") token: String
    ): Response<BehaviorAnalysisSimpleResponse>
    
    /**
     * Returns coarse pattern labels detected in recent behavior for a user.
     */
    @GET("behavior-analysis/patterns/{userId}")
    suspend fun getDetectedPatterns(
        @Path("userId") userId: String,
        @Query("days") days: Int = 7,
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>
    
    /**
     * Deletes stored behavior analytics for the given user id.
     */
    @DELETE("behavior-analysis/clear/{userId}")
    suspend fun clearUserBehaviorData(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<BehaviorAnalysisSimpleResponse>
}
