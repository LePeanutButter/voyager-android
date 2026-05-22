package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Request payload for sending a chat message to the AI assistant service.
 */
@JsonClass(generateAdapter = true)
data class AiChatRequestDto(
    @Json(name = "userId") val userId: String,
    @Json(name = "message") val message: String,
)

/**
 * Assistant reply text returned for a chat turn.
 */
@JsonClass(generateAdapter = true)
data class AiChatReplyDto(
    @Json(name = "reply") val reply: String,
)

/**
 * Geographic hint (coordinates and optional city label) used in AI recommendation bodies (snake_case JSON).
 */
@JsonClass(generateAdapter = true)
data class AiLocationBody(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "city") val city: String? = null,
)

/**
 * Candidato enviado al ranking [POST /local/recommendations] (snake_case, mismo contrato que Postman / web).
 */
@JsonClass(generateAdapter = true)
data class LocalRecommendationCandidateBody(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "category") val category: String,
    @Json(name = "price") val price: Double = 0.0,
    @Json(name = "content_text") val contentText: String = "",
)

/**
 * Cuerpo [POST /local/recommendations] — el servicio rankea candidatos del cliente (no catálogo legacy HTTP).
 */
@JsonClass(generateAdapter = true)
data class LocalRecommendationRequestBody(
    @Json(name = "user_id") val userId: String,
    @Json(name = "query") val query: String,
    @Json(name = "limit") val limit: Int = 8,
    @Json(name = "candidates") val candidates: List<LocalRecommendationCandidateBody>,
)

/** Respuesta [POST /local/recommendations] (con lista de 'items'). */
@JsonClass(generateAdapter = true)
data class LocalRecommendationResponseDto(
    @Json(name = "items") val items: List<LocalRecommendationItemDto> = emptyList(),
    @Json(name = "user") val user: Map<String, Any>? = null,
    @Json(name = "preferences") val preferences: List<String> = emptyList(),
)

/** Item rankeado en la respuesta de recomendaciones locales. */
@JsonClass(generateAdapter = true)
data class LocalRecommendationItemDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "category") val category: String,
    @Json(name = "price") val price: Double = 0.0,
    @Json(name = "score") val score: Double = 0.0,
    @Json(name = "similarity") val similarity: Double = 0.0,
    @Json(name = "content_text") val contentText: String? = null,
)

/** Cuerpo [POST /local/chat/message] (mismo contrato que `useAIChat` en el web). */
@JsonClass(generateAdapter = true)
data class LocalChatRequestBody(
    @Json(name = "user_id") val userId: String,
    @Json(name = "session_id") val sessionId: String,
    @Json(name = "message") val message: String,
)

/** Respuesta mínima del chatbot local FastAPI. */
@JsonClass(generateAdapter = true)
data class LocalChatResponseDto(
    @Json(name = "session_id") val sessionId: String = "",
    @Json(name = "reply") val reply: String = "",
    @Json(name = "role") val role: String? = null,
)

/**
 * Wrapper for chat history response when returned as an object (Expected BEGIN_ARRAY but was BEGIN_OBJECT).
 */
@JsonClass(generateAdapter = true)
data class LocalChatHistoryResponseDto(
    @Json(name = "messages") val messages: List<LocalChatMessageDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class LocalChatMessageDto(
    @Json(name = "role") val role: String = "",
    @Json(name = "content") val content: String = "",
    @Json(name = "message") val message: String = "",
    @Json(name = "session_id") val sessionId: String? = null
)

/**
 * Body for ranking compatible travelers around a location and optional trip window.
 */
@JsonClass(generateAdapter = true)
data class AiTravelerMatchRequestBody(
    @Json(name = "user_id") val userId: String,
    @Json(name = "location") val location: AiLocationBody,
    @Json(name = "travel_dates") val travelDates: Map<String, String>? = null,
    @Json(name = "preferences") val preferences: List<String>? = null,
    @Json(name = "max_matches") val maxMatches: Int = 10,
)

/**
 * Body for reporting the outcome of a connection attempt for learning or ranking.
 */
@JsonClass(generateAdapter = true)
data class AiConnectionOutcomeRequestBody(
    @Json(name = "user_id") val userId: String,
    @Json(name = "target_user_id") val targetUserId: String,
    @Json(name = "outcome") val outcome: String,
    @Json(name = "dimension_snapshot") val dimensionSnapshot: Map<String, Double>? = null,
    @Json(name = "notes") val notes: String? = null,
)

/**
 * Full synthetic profile block sent to AI endpoints that expect nested preferences and history.
 */
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

/**
 * Preference dimensions nested under [AiUserProfileBody] or update requests.
 */
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

/**
 * Partial profile update: only fields present are applied server-side.
 */
@JsonClass(generateAdapter = true)
data class AiUserProfileUpdateBody(
    @Json(name = "preferences") val preferences: AiUserPreferencesBody? = null,
    @Json(name = "location") val location: String? = null,
    @Json(name = "travel_history") val travelHistory: List<Map<String, String>>? = null,
)

/**
 * Single user–activity interaction event for AI preference learning.
 */
@JsonClass(generateAdapter = true)
data class AiUserInteractionBody(
    @Json(name = "user_id") val userId: String,
    @Json(name = "activity_id") val activityId: String,
    @Json(name = "interaction_type") val interactionType: String,
    @Json(name = "metadata") val metadata: Map<String, String>? = null,
)

/**
 * Request seasonal demand or visibility forecast for a destination over a window of months.
 */
@JsonClass(generateAdapter = true)
data class AiSeasonalForecastRequestBody(
    @Json(name = "destination_id") val destinationId: String,
    @Json(name = "start_month") val startMonth: Int,
    @Json(name = "horizon_months") val horizonMonths: Int = 6,
)

/**
 * Request ranking or visibility adjustments for multiple destinations in a given travel month.
 */
@JsonClass(generateAdapter = true)
data class AiVisibilityAdjustmentsRequestBody(
    @Json(name = "destination_ids") val destinationIds: List<String>,
    @Json(name = "travel_month") val travelMonth: Int,
    @Json(name = "apply_mitigation") val applyMitigation: Boolean = true,
)
