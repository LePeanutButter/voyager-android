package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDateTime

/**
 * Payload sent when recording a single implicit user interaction for analysis.
 */
@JsonClass(generateAdapter = true)
data class BehaviorTrackingRequest(
    @Json(name = "user_id") 
    val userId: String,
    
    @Json(name = "interaction_type")
    val interactionType: InteractionType,
    
    @Json(name = "activity_id")
    val activityId: String? = null,
    
    @Json(name = "activity_category")
    val activityCategory: String? = null,
    
    @Json(name = "session_duration")
    val sessionDuration: Int? = null, // seconds
    
    @Json(name = "context")
    val context: Map<String, Any> = emptyMap()
)

/**
 * Request body for running a behavior analysis job over a configurable time window.
 */
@JsonClass(generateAdapter = true)
data class BehaviorAnalysisRequest(
    @Json(name = "user_id")
    val userId: String,
    
    @Json(name = "analysis_period_days")
    val analysisPeriodDays: Int = 7,
    
    @Json(name = "include_patterns")
    val includePatterns: Boolean = true,
    
    @Json(name = "include_preference_updates")
    val includePreferenceUpdates: Boolean = true
)

/**
 * Response describing inferred preference changes, detected patterns, and confidence from an analysis run.
 */
@JsonClass(generateAdapter = true)
data class ImplicitPreferenceUpdate(
    @Json(name = "user_id")
    val userId: String,
    
    @Json(name = "preference_changes")
    val preferenceChanges: Map<String, Double>,
    
    @Json(name = "detected_patterns")
    val detectedPatterns: List<BehaviorPattern>,
    
    @Json(name = "analysis_period")
    val analysisPeriod: DateRange,
    
    @Json(name = "confidence_score")
    val confidenceScore: Double
)

/**
 * One recurring behavior pattern identified for the user, with confidence and recency metadata.
 */
@JsonClass(generateAdapter = true)
data class BehaviorPattern(
    @Json(name = "pattern_type")
    val patternType: String,
    
    @Json(name = "confidence")
    val confidence: Double,
    
    @Json(name = "frequency")
    val frequency: Int,
    
    @Json(name = "last_detected")
    val lastDetected: LocalDateTime,
    
    @Json(name = "context")
    val context: Map<String, Any>
)

/**
 * Inclusive analysis window in local date-time coordinates from the backend.
 */
@JsonClass(generateAdapter = true)
data class DateRange(
    @Json(name = "start")
    val start: LocalDateTime,
    
    @Json(name = "end")
    val end: LocalDateTime
)

/**
 * Aggregated counters and recent patterns for dashboard or summary endpoints.
 */
@JsonClass(generateAdapter = true)
data class BehaviorSummary(
    @Json(name = "user_id")
    val userId: String,
    
    @Json(name = "analysis_period_days")
    val analysisPeriodDays: Int,
    
    @Json(name = "total_interactions")
    val totalInteractions: Int,
    
    @Json(name = "interaction_breakdown")
    val interactionBreakdown: Map<String, Int>,
    
    @Json(name = "category_breakdown")
    val categoryBreakdown: Map<String, Int>,
    
    @Json(name = "recent_patterns")
    val recentPatterns: List<BehaviorPattern>,
    
    @Json(name = "last_analysis")
    val lastAnalysis: LocalDateTime?
)

/**
 * Simple success or error envelope returned by the behavior-analysis service (distinct from [ApiResponse]).
 */
@JsonClass(generateAdapter = true)
data class BehaviorAnalysisSimpleResponse(
    @Json(name = "success")
    val success: Boolean,
    
    @Json(name = "message")
    val message: String,
    
    @Json(name = "data")
    val data: Map<String, Any>? = null
)

/**
 * Interaction kinds allowed when tracking user behavior for analytics.
 */
enum class InteractionType {
    @Json(name = "view")
    VIEW,
    
    @Json(name = "click")
    CLICK,
    
    @Json(name = "bookmark")
    BOOKMARK,
    
    @Json(name = "share")
    SHARE,
    
    @Json(name = "reject")
    REJECT,
    
    @Json(name = "book")
    BOOK,
    
    @Json(name = "rate")
    RATE,
    
    @Json(name = "search")
    SEARCH,
    
    @Json(name = "filter")
    FILTER
}
