package com.voyager.tourism.domain.repository

import com.voyager.tourism.data.dto.*

/**
 * Domain repository interface for Behavior Analysis feature
 * Defines contract for behavior tracking and analysis operations
 */
interface BehaviorAnalysisRepository {
    
    /**
     * Track a user interaction for behavior analysis
     */
    suspend fun trackInteraction(
        userId: String,
        interactionType: InteractionType,
        activityId: String? = null,
        activityCategory: String? = null,
        sessionDuration: Int? = null,
        context: Map<String, Any> = emptyMap()
    ): Result<ApiResponse>
    
    /**
     * Analyze user behavior patterns and generate preference updates
     */
    suspend fun analyzeUserBehavior(
        userId: String,
        analysisPeriodDays: Int = 7,
        includePatterns: Boolean = true,
        includePreferenceUpdates: Boolean = true
    ): Result<ImplicitPreferenceUpdate>
    
    /**
     * Get a summary of user's behavior patterns
     */
    suspend fun getBehaviorSummary(
        userId: String,
        days: Int = 30
    ): Result<BehaviorSummary>
    
    /**
     * Track multiple user interactions in a batch
     */
    suspend fun batchTrackInteractions(
        requests: List<BehaviorTrackingRequest>
    ): Result<ApiResponse>
    
    /**
     * Get detected behavior patterns for a user
     */
    suspend fun getDetectedPatterns(
        userId: String,
        days: Int = 7
    ): Result<Map<String, Any>>
    
    /**
     * Clear all behavior data for a user
     */
    suspend fun clearUserBehaviorData(userId: String): Result<ApiResponse>
}
