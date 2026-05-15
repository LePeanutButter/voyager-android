package com.voyager.tourism.data.api

import com.voyager.tourism.data.dto.AiChatRequestDto
import com.voyager.tourism.data.dto.AiChatReplyDto
import com.voyager.tourism.data.dto.AiAdaptiveMenuDto
import com.voyager.tourism.data.dto.AiHomeFeedDto
import com.voyager.tourism.data.dto.AiMatchingResponseDto
import com.voyager.tourism.data.dto.AiSeasonalityOverviewDto
import com.voyager.tourism.data.dto.AiTrendDashboardDto
import com.voyager.tourism.data.dto.ConnectionDto
import com.voyager.tourism.data.dto.AiConnectionOutcomeRequestBody
import com.voyager.tourism.data.dto.AiSeasonalForecastRequestBody
import com.voyager.tourism.data.dto.AiTravelerMatchRequestBody
import com.voyager.tourism.data.dto.AiUserInteractionBody
import com.voyager.tourism.data.dto.AiUserPreferencesBody
import com.voyager.tourism.data.dto.AiUserProfileBody
import com.voyager.tourism.data.dto.AiUserProfileUpdateBody
import com.voyager.tourism.data.dto.AiVisibilityAdjustmentsRequestBody
import com.voyager.tourism.data.dto.LocalChatRequestBody
import com.voyager.tourism.data.dto.LocalChatResponseDto
import com.voyager.tourism.data.dto.LocalRecommendationRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * FastAPI microservice bajo `/api/v1`: matching, tendencias, estacionalidad, chat, **local** (ranking con candidatos),
 * perfil IA y UI adaptativa.
 *
 * Los endpoints históricos bajo el prefijo recommendations (p. ej. personalized, trending) fueron retirados del servicio;
 * el flujo equivalente es postLocalRecommendations y postLocalRecommendationFeedback (ver Postman y aiService del cliente web).
 */
interface VoyagerAiApi {

    // --- local (ranking con candidatos del cliente; alineado con web) ---
    /** Rankea candidatos reales enviados por el cliente (sustituye el antiguo router HTTP bajo recommendations). */
    @POST("local/recommendations")
    suspend fun postLocalRecommendations(
        @Body body: LocalRecommendationRequestBody,
    ): Response<AiMatchingResponseDto>

    /** Feedback 1–5 para ítems rankeados por [postLocalRecommendations]. */
    @POST("local/recommendations/feedback")
    suspend fun postLocalRecommendationFeedback(
        @Query("user_id") userId: String,
        @Query("item_id") itemId: String,
        @Query("rating") rating: Int,
    ): Response<Unit>

    /** Turno de chat local (Ollama / SQLite) alineado con `aiService.sendLocalChatMessage`. */
    @POST("local/chat/message")
    suspend fun postLocalChatMessage(@Body body: LocalChatRequestBody): Response<LocalChatResponseDto>

    /** Historial de mensajes por sesión (`useAIChat` / `getLocalChatHistory` en el web).
     * Prefer typed list of `LocalChatResponseDto` for strong typing on the client.
     */
    @GET("local/chat/history/{session_id}")
    suspend fun getLocalChatHistory(
        @Path("session_id") sessionId: String,
        @Query("limit") limit: Int = 50,
    ): Response<List<com.voyager.tourism.data.dto.LocalChatResponseDto>>

    // --- users (perfil IA) ---
    /** Bootstraps an AI-side user profile mirror. */
    @POST("users/profile")
    suspend fun postUserProfile(@Body body: AiUserProfileBody): Response<Unit>

    /** Retrieves AI enrichment data for a user id. */
    @GET("users/profile/{user_id}")
    suspend fun getUserProfile(@Path("user_id") userId: String): Response<AiUserProfileBody>

    /** Overwrites mutable AI profile fields. */
    @PUT("users/profile/{user_id}")
    suspend fun putUserProfile(
        @Path("user_id") userId: String,
        @Body body: AiUserProfileUpdateBody,
    ): Response<Unit>

    /** Sends compact preference vectors learned from UI interactions. */
    @POST("users/preferences/{user_id}")
    suspend fun postUserPreferences(
        @Path("user_id") userId: String,
        @Body body: AiUserPreferencesBody,
    ): Response<Unit>

    /** Notifies the service about a user gesture for modeling. */
    @POST("users/interaction")
    suspend fun postUserInteraction(@Body body: AiUserInteractionBody): Response<Unit>

    /** Streams the latest ranked interactions for analytics views. */
    @GET("users/history/{user_id}")
    suspend fun getUserInteractionHistory(
        @Path("user_id") userId: String,
        @Query("limit") limit: Int = 50,
    ): Response<List<AiUserInteractionBody>>

    /** Fetches condensed insight statements for coaching UI surfaces. */
    @GET("users/insights/{user_id}")
    suspend fun getUserInsights(@Path("user_id") userId: String): Response<Map<String, Any>>

    /** Deletes mirrored AI data for GDPR or account closure flows. */
    @DELETE("users/profile/{user_id}")
    suspend fun deleteUserProfile(@Path("user_id") userId: String): Response<Unit>

    // --- matching ---
    /** Runs the AI traveler matching pipeline with rich filters. */
    @POST("matching/find")
    suspend fun postMatchingFind(@Body body: AiTravelerMatchRequestBody): Response<AiMatchingResponseDto>

    /** Returns a compatibility score between two Voyager user ids. */
    @GET("matching/compatibility/{user_id}/{target_user_id}")
    suspend fun getMatchingCompatibility(
        @Path("user_id") userId: String,
        @Path("target_user_id") targetUserId: String,
    ): Response<Map<String, Double>>

    /** Initiates a match connection with an optional ice-breaker message. */
    @POST("matching/connect/{user_id}/{target_user_id}")
    suspend fun postMatchingConnect(
        @Path("user_id") userId: String,
        @Path("target_user_id") targetUserId: String,
        @Query("message") message: String? = null,
    ): Response<Unit>

    /** Lists pending or accepted AI-suggested connections for review. */
    @GET("matching/connections/{user_id}")
    suspend fun getMatchingConnections(
        @Path("user_id") userId: String,
        @Query("status") status: String? = null,
    ): Response<List<ConnectionDto>>

    /** Accepts or declines an AI match suggestion by connection id. */
    @PUT("matching/connections/{connection_id}/respond")
    suspend fun putMatchingConnectionRespond(
        @Path("connection_id") connectionId: String,
        @Query("response") response: String,
        @Query("message") message: String? = null,
    ): Response<Unit>

    /**
     * Sugiere compañeros de viaje; [seekerFootprint] es opcional (coma-separado, mismo query que el web:
     * `seeker_footprint` en FastAPI).
     */
    @GET("matching/recommendations/{user_id}")
    suspend fun getTravelBuddyRecommendations(
        @Path("user_id") userId: String,
        @Query("location") location: String? = null,
        @Query("limit") limit: Int = 10,
        @Query("seeker_footprint") seekerFootprint: String? = null,
    ): Response<AiMatchingResponseDto>

    /** Logs whether a suggestion converted to a real-world meetup. */
    @POST("matching/learning/connection-outcome")
    suspend fun postConnectionOutcome(@Body body: AiConnectionOutcomeRequestBody): Response<Unit>

    /** Captures qualitative feedback about a proposed match pairing. */
    @POST("matching/feedback/{user_id}/{target_user_id}")
    suspend fun postMatchFeedback(
        @Path("user_id") userId: String,
        @Path("target_user_id") targetUserId: String,
        @Query("rating") rating: Int,
        @Query("feedback_text") feedbackText: String? = null,
    ): Response<Unit>

    // --- trends ---
    /** Aggregated KPIs for executive trend dashboards. */
    @GET("trends/dashboard")
    suspend fun getTrendsDashboard(): Response<AiTrendDashboardDto>

    /** Deep dive metrics for a named marketing segment. */
    @GET("trends/segments/{segment_id}/insights")
    suspend fun getSegmentInsights(@Path("segment_id") segmentId: String): Response<Map<String, Any>>

    /** Weekly narrative digest for newsletter or push notifications. */
    @GET("trends/weekly-digest")
    suspend fun getWeeklyDigest(): Response<Map<String, Any>>

    // --- seasonality ---
    /** Overview of seasonal demand curves with optional pivot month. */
    @GET("seasonality/overview")
    suspend fun getSeasonalityOverview(@Query("reference_month") referenceMonth: Int? = null): Response<AiSeasonalityOverviewDto>

    /** Forecast notes for a specific destination identifier. */
    @GET("seasonality/destinations/{destination_id}")
    suspend fun getDestinationSeasonalProfile(@Path("destination_id") destinationId: String): Response<Map<String, Any>>

    /** Runs a seasonal demand projection with structured inputs. */
    @POST("seasonality/forecast")
    suspend fun postSeasonalForecast(@Body body: AiSeasonalForecastRequestBody): Response<Map<String, Any>>

    /** Applies AI-proposed ranking boosts for shoulder seasons. */
    @POST("seasonality/visibility-adjustments")
    suspend fun postVisibilityAdjustments(@Body body: AiVisibilityAdjustmentsRequestBody): Response<Unit>

    // --- chat ---
    /** Sends a user utterance to the guided travel assistant. */
    @POST("chat")
    suspend fun postChat(@Body body: AiChatRequestDto): Response<AiChatReplyDto>

    /** Retrieves recent chat turns for continuity in the UI. */
    @GET("chat/{user_id}/history")
    suspend fun getChatHistory(@Path("user_id") userId: String): Response<List<AiChatReplyDto>>

    /** Clears stored chat transcripts for privacy resets. */
    @DELETE("chat/{user_id}/history")
    suspend fun deleteChatHistory(@Path("user_id") userId: String): Response<Unit>

    // --- adaptive UI ---
    /** Builds a role-aware menu tree for adaptive shells. */
    @GET("adaptive-ui/menu/{user_id}")
    suspend fun getAdaptiveMenu(@Path("user_id") userId: String): Response<AiAdaptiveMenuDto>

    /** Home feed cards curated from AI ranking signals. */
    @GET("adaptive-ui/home-feed/{user_id}")
    suspend fun getAdaptiveHomeFeed(@Path("user_id") userId: String): Response<AiHomeFeedDto>

    // --- service diagnostics (absolute paths on host) ---
    /** Public landing JSON describing the FastAPI service. */
    @GET("/")
    suspend fun getServiceRoot(): Response<Map<String, Any>>

    /** Lightweight readiness probe for uptime monitors. */
    @GET("/health")
    suspend fun getHealth(): Response<Map<String, Any>>

    // --- ingest / admin (exposed by web client's aiService; mobile rarely uses these,
    // but expose them here behind admin callers if needed) ---
    @POST("trends/ingest/signals")
    suspend fun ingestTrendSignals(@Body body: Map<String, Any>): Response<Unit>

    @POST("trends/ingest/segments")
    suspend fun ingestTrendSegments(@Body body: Map<String, Any>): Response<Unit>

    @POST("seasonality/ingest/profiles")
    suspend fun ingestSeasonalityProfiles(@Body body: Map<String, Any>): Response<Unit>

    @POST("matching/profiles/ingest")
    suspend fun ingestMatchingProfiles(@Body body: Map<String, Any>): Response<Unit>
}

