package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Collection of matches returned by the AI matching service.
 */
@JsonClass(generateAdapter = true)
data class AiMatchingResponseDto(
    @Json(name = "matches") val matches: List<AiTravelerMatchDto>,
    @Json(name = "userId") val userId: String,
    @Json(name = "totalMatches") val totalMatches: Int,
    @Json(name = "generatedAt") val generatedAt: String? = null
)

/**
 * One ranked traveler candidate with compatibility breakdown.
 */
@JsonClass(generateAdapter = true)
data class AiTravelerMatchDto(
    @Json(name = "userId") val userId: String,
    @Json(name = "name") val name: String,
    @Json(name = "age") val age: Int? = null,
    @Json(name = "compatibilityScore") val compatibilityScore: Double,
    @Json(name = "commonPreferences") val commonPreferences: List<String> = emptyList(),
    @Json(name = "travelStyleMatch") val travelStyleMatch: Double,
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "profileImage") val profileImage: String? = null,
    @Json(name = "dimensionSummary") val dimensionSummary: Map<String, Double>? = null,
    @Json(name = "sharedDestinations") val sharedDestinations: List<String> = emptyList()
)

/**
 * Aggregated KPIs for trend dashboards.
 */
@JsonClass(generateAdapter = true)
data class AiTrendDashboardDto(
    @Json(name = "trendingDestinations") val trendingDestinations: List<TrendItemDto> = emptyList(),
    @Json(name = "popularActivities") val popularActivities: List<TrendItemDto> = emptyList(),
    @Json(name = "emergingSegments") val emergingSegments: List<String> = emptyList(),
    @Json(name = "lastUpdated") val lastUpdated: String? = null
)

@JsonClass(generateAdapter = true)
data class TrendItemDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "growthRate") val growthRate: Double,
    @Json(name = "score") val score: Double
)

/**
 * Overview of seasonal demand curves.
 */
@JsonClass(generateAdapter = true)
data class AiSeasonalityOverviewDto(
    @Json(name = "referenceMonth") val referenceMonth: Int = 1,
    @Json(name = "curves") val curves: Map<String, List<Double>> = emptyMap(),
    @Json(name = "peakDestinations") val peakDestinations: List<String> = emptyList(),
    @Json(name = "shoulderDestinations") val shoulderDestinations: List<String> = emptyList()
)

/**
 * Role-aware menu tree for adaptive UI.
 */
@JsonClass(generateAdapter = true)
data class AiAdaptiveMenuDto(
    @Json(name = "userId") val userId: String,
    @Json(name = "items") val items: List<AdaptiveMenuItemDto>,
    @Json(name = "layoutVersion") val layoutVersion: String
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
    @Json(name = "userId") val userId: String,
    @Json(name = "cards") val cards: List<AiFeedCardDto>
)

@JsonClass(generateAdapter = true)
data class AiFeedCardDto(
    @Json(name = "id") val id: String,
    @Json(name = "type") val type: String, // e.g., "RECOMMENDATION", "TREND", "MATCH"
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String,
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "metadata") val metadata: Map<String, String>? = null
)
