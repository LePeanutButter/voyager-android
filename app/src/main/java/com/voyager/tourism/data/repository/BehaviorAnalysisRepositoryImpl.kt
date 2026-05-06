package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.BehaviorAnalysisApi
import com.voyager.tourism.data.dto.*
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.BehaviorAnalysisRepository
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation for Behavior Analysis feature
 * Handles data flow between API and local storage
 */
@Singleton
class BehaviorAnalysisRepositoryImpl @Inject constructor(
    private val behaviorAnalysisApi: BehaviorAnalysisApi,
    private val preferencesManager: PreferencesManager
) : BehaviorAnalysisRepository {
    
    override suspend fun trackInteraction(
        userId: String,
        interactionType: InteractionType,
        activityId: String?,
        activityCategory: String?,
        sessionDuration: Int?,
        context: Map<String, Any>
    ): Result<ApiResponse> {
        return try {
            val token = preferencesManager.getAuthToken() ?: throw Exception("Not authenticated")
            
            val request = BehaviorTrackingRequest(
                userId = userId,
                interactionType = interactionType,
                activityId = activityId,
                activityCategory = activityCategory,
                sessionDuration = sessionDuration,
                context = context
            )
            
            val response = behaviorAnalysisApi.trackUserBehavior(request, "Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to track behavior: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun analyzeUserBehavior(
        userId: String,
        analysisPeriodDays: Int,
        includePatterns: Boolean,
        includePreferenceUpdates: Boolean
    ): Result<ImplicitPreferenceUpdate> {
        return try {
            val token = preferencesManager.getAuthToken() ?: throw Exception("Not authenticated")
            
            val request = BehaviorAnalysisRequest(
                userId = userId,
                analysisPeriodDays = analysisPeriodDays,
                includePatterns = includePatterns,
                includePreferenceUpdates = includePreferenceUpdates
            )
            
            val response = behaviorAnalysisApi.analyzeUserBehavior(request, "Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to analyze behavior: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getBehaviorSummary(
        userId: String,
        days: Int
    ): Result<BehaviorSummary> {
        return try {
            val token = preferencesManager.getAuthToken() ?: throw Exception("Not authenticated")
            
            val response = behaviorAnalysisApi.getBehaviorSummary(userId, days, "Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get behavior summary: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun batchTrackInteractions(
        requests: List<BehaviorTrackingRequest>
    ): Result<ApiResponse> {
        return try {
            val token = preferencesManager.getAuthToken() ?: throw Exception("Not authenticated")
            
            val response = behaviorAnalysisApi.batchTrackBehavior(requests, "Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to batch track behavior: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getDetectedPatterns(
        userId: String,
        days: Int
    ): Result<Map<String, Any>> {
        return try {
            val token = preferencesManager.getAuthToken() ?: throw Exception("Not authenticated")
            
            val response = behaviorAnalysisApi.getDetectedPatterns(userId, days, "Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get detected patterns: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun clearUserBehaviorData(userId: String): Result<ApiResponse> {
        return try {
            val token = preferencesManager.getAuthToken() ?: throw Exception("Not authenticated")
            
            val response = behaviorAnalysisApi.clearUserBehaviorData(userId, "Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to clear behavior data: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
