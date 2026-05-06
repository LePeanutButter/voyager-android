package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDateTime

/**
 * Data Transfer Objects for Behavior Analysis feature
 * Matches backend API contracts for implicit user behavior tracking
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

@JsonClass(generateAdapter = true)
data class DateRange(
    @Json(name = "start")
    val start: LocalDateTime,
    
    @Json(name = "end")
    val end: LocalDateTime
)

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

/** Respuesta genérica del servicio de análisis de comportamiento (no confundir con [ApiResponse]). */
@JsonClass(generateAdapter = true)
data class BehaviorAnalysisSimpleResponse(
    @Json(name = "success")
    val success: Boolean,
    
    @Json(name = "message")
    val message: String,
    
    @Json(name = "data")
    val data: Map<String, Any>? = null
)

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
