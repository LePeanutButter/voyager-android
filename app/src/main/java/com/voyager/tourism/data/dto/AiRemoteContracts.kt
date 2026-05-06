package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// --- Chat ---
@JsonClass(generateAdapter = true)
data class AiChatRequestDto(
    @Json(name = "userId") val userId: String,
    @Json(name = "message") val message: String,
)

@JsonClass(generateAdapter = true)
data class AiChatReplyDto(
    @Json(name = "reply") val reply: String,
)

// --- Recommendations (cuerpos Pydantic en snake_case) ---
@JsonClass(generateAdapter = true)
data class AiLocationBody(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "city") val city: String? = null,
)

@JsonClass(generateAdapter = true)
data class AiRecommendationRequestBody(
    @Json(name = "user_id") val userId: String,
    @Json(name = "location") val location: AiLocationBody,
    @Json(name = "preferences") val preferences: List<String>? = null,
    @Json(name = "max_results") val maxResults: Int = 10,
    @Json(name = "date_range") val dateRange: Map<String, String>? = null,
    @Json(name = "group_size") val groupSize: Int? = null,
    @Json(name = "budget_limit") val budgetLimit: Double? = null,
)

@JsonClass(generateAdapter = true)
data class AiDestinationRecommendationRequestBody(
    @Json(name = "user_id") val userId: String,
    @Json(name = "max_results") val maxResults: Int = 8,
    @Json(name = "prefer_successful_patterns") val preferSuccessfulPatterns: Boolean = true,
    @Json(name = "include_emerging_trends") val includeEmergingTrends: Boolean = true,
    @Json(name = "theme_weights") val themeWeights: Map<String, Double>? = null,
    @Json(name = "travel_month") val travelMonth: Int? = null,
    @Json(name = "apply_seasonality_mitigation") val applySeasonalityMitigation: Boolean = true,
)

@JsonClass(generateAdapter = true)
data class AiContextualActivityRequestBody(
    @Json(name = "user_id") val userId: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "city_hint") val cityHint: String? = null,
    @Json(name = "weather") val weather: String = "UNKNOWN",
    @Json(name = "max_results") val maxResults: Int = 8,
    @Json(name = "radius_km") val radiusKm: Double = 25.0,
)

@JsonClass(generateAdapter = true)
data class AiTravelerMatchRequestBody(
    @Json(name = "user_id") val userId: String,
    @Json(name = "location") val location: AiLocationBody,
    @Json(name = "travel_dates") val travelDates: Map<String, String>? = null,
    @Json(name = "preferences") val preferences: List<String>? = null,
    @Json(name = "max_matches") val maxMatches: Int = 10,
)

@JsonClass(generateAdapter = true)
data class AiConnectionOutcomeRequestBody(
    @Json(name = "user_id") val userId: String,
    @Json(name = "target_user_id") val targetUserId: String,
    @Json(name = "outcome") val outcome: String,
    @Json(name = "dimension_snapshot") val dimensionSnapshot: Map<String, Double>? = null,
    @Json(name = "notes") val notes: String? = null,
)

@JsonClass(generateAdapter = true)
data class AiUserProfileBody(
    @Json(name = "user_id") val userId: String,
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "age") val age: Int? = null,
    @Json(name = "location") val location: String? = null,
    @Json(name = "preferences") val preferences: AiUserPreferencesBody,
    @Json(name = "travel_history") val travelHistory: List<Map<String, String>> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class AiUserPreferencesBody(
    @Json(name = "preferences") val preferences: List<String> = emptyList(),
    @Json(name = "budget_range") val budgetRange: Map<String, Double> = mapOf("min" to 50.0, "max" to 200.0),
    @Json(name = "travel_style") val travelStyle: String = "mid-range",
    @Json(name = "group_size") val groupSize: Int = 2,
    @Json(name = "accessibility_needs") val accessibilityNeeds: List<String> = emptyList(),
    @Json(name = "dietary_restrictions") val dietaryRestrictions: List<String> = emptyList(),
    @Json(name = "language_preferences") val languagePreferences: List<String> = listOf("English"),
)

@JsonClass(generateAdapter = true)
data class AiUserProfileUpdateBody(
    @Json(name = "preferences") val preferences: AiUserPreferencesBody? = null,
    @Json(name = "location") val location: String? = null,
    @Json(name = "travel_history") val travelHistory: List<Map<String, String>>? = null,
)

@JsonClass(generateAdapter = true)
data class AiUserInteractionBody(
    @Json(name = "user_id") val userId: String,
    @Json(name = "activity_id") val activityId: String,
    @Json(name = "interaction_type") val interactionType: String,
    @Json(name = "metadata") val metadata: Map<String, String>? = null,
)

@JsonClass(generateAdapter = true)
data class AiSeasonalForecastRequestBody(
    @Json(name = "destination_id") val destinationId: String,
    @Json(name = "start_month") val startMonth: Int,
    @Json(name = "horizon_months") val horizonMonths: Int = 6,
)

@JsonClass(generateAdapter = true)
data class AiVisibilityAdjustmentsRequestBody(
    @Json(name = "destination_ids") val destinationIds: List<String>,
    @Json(name = "travel_month") val travelMonth: Int,
    @Json(name = "apply_mitigation") val applyMitigation: Boolean = true,
)
