package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Collection of matches returned by the AI matching service.
 */
@JsonClass(generateAdapter = true)
data class AiMatchingResponseDto(
    @Json(name = "matches") val matches: List<AiTravelerMatchDto>,
    @Json(name = "user_id") val userId: String,
    @Json(name = "total_matches") val totalMatches: Int,
    @Json(name = "generated_at") val generatedAt: String? = null
)

/**
 * One ranked traveler candidate with compatibility breakdown.
 */
@JsonClass(generateAdapter = true)
data class AiTravelerMatchDto(
    @Json(name = "user_id") val userId: String,
    @Json(name = "name") val name: String,
    @Json(name = "age") val age: Int? = null,
    @Json(name = "compatibility_score") val compatibilityScore: Double,
    @Json(name = "common_preferences") val commonPreferences: List<String> = emptyList(),
    @Json(name = "travel_style_match") val travelStyleMatch: Double,
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "profile_image") val profileImage: String? = null,
    @Json(name = "dimension_summary") val dimensionSummary: Map<String, Double>? = null,
    @Json(name = "shared_destinations") val sharedDestinations: List<String> = emptyList()
)

/**
 * Aggregated KPIs for trend dashboards.
 */
@JsonClass(generateAdapter = true)
data class AiTrendDashboardDto(
    @Json(name = "trending_destinations") val trendingDestinations: List<TrendItemDto>,
    @Json(name = "popular_activities") val popularActivities: List<TrendItemDto>,
    @Json(name = "emerging_segments") val emergingSegments: List<String>,
    @Json(name = "last_updated") val lastUpdated: String? = null
)

@JsonClass(generateAdapter = true)
data class TrendItemDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "growth_rate") val growthRate: Double,
    @Json(name = "score") val score: Double
)

/**
 * Overview of seasonal demand curves.
 */
@JsonClass(generateAdapter = true)
data class AiSeasonalityOverviewDto(
    @Json(name = "reference_month") val referenceMonth: Int,
    @Json(name = "curves") val curves: Map<String, List<Double>>,
    @Json(name = "peak_destinations") val peakDestinations: List<String>,
    @Json(name = "shoulder_destinations") val shoulderDestinations: List<String>
)

/**
 * Role-aware menu tree for adaptive UI.
 */
@JsonClass(generateAdapter = true)
data class AiAdaptiveMenuDto(
    @Json(name = "user_id") val userId: String,
    @Json(name = "items") val items: List<AdaptiveMenuItemDto>,
    @Json(name = "layout_version") val layoutVersion: String
)

@JsonClass(generateAdapter = true)
data class AdaptiveMenuItemDto(
    @Json(name = "id") val id: String,
    @Json(name = "label") val label: String,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "route") val route: String,
    @Json(name = "priority") val priority: Int,
    @Json(name = "visible") val visible: Boolean = true
)

/**
 * Home feed cards curated from AI ranking signals.
 */
@JsonClass(generateAdapter = true)
data class AiHomeFeedDto(
    @Json(name = "user_id") val userId: String,
    @Json(name = "cards") val cards: List<AiFeedCardDto>
)

@JsonClass(generateAdapter = true)
data class AiFeedCardDto(
    @Json(name = "id") val id: String,
    @Json(name = "type") val type: String, // e.g., "RECOMMENDATION", "TREND", "MATCH"
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "metadata") val metadata: Map<String, String>? = null
)
