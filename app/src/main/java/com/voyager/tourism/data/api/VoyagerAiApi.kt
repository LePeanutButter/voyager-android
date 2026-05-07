package com.voyager.tourism.data.api

import com.voyager.tourism.data.dto.AiChatRequestDto
import com.voyager.tourism.data.dto.AiChatReplyDto
import com.voyager.tourism.data.dto.AiConnectionOutcomeRequestBody
import com.voyager.tourism.data.dto.AiContextualActivityRequestBody
import com.voyager.tourism.data.dto.AiDestinationRecommendationRequestBody
import com.voyager.tourism.data.dto.AiRecommendationRequestBody
import com.voyager.tourism.data.dto.AiSeasonalForecastRequestBody
import com.voyager.tourism.data.dto.AiTravelerMatchRequestBody
import com.voyager.tourism.data.dto.AiUserInteractionBody
import com.voyager.tourism.data.dto.AiUserPreferencesBody
import com.voyager.tourism.data.dto.AiUserProfileBody
import com.voyager.tourism.data.dto.AiUserProfileUpdateBody
import com.voyager.tourism.data.dto.AiVisibilityAdjustmentsRequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.ResponseBody

/**
 * FastAPI microservice surface under `/api/v1` for AI recommendations, traveler matching, trends, seasonality, and chat.
 * Questionnaire and behavior flows stay on [AiTravelPreferencesApi] and [BehaviorAnalysisApi].
 * Dynamic JSON shapes are exposed as [ResponseBody] to keep cloud deployments tolerant of schema drift.
 */
interface VoyagerAiApi {

    // --- recommendations ---
    /** Returns personalized destination ideas tailored to the provided context. */
    @POST("recommendations/destinations/personalized")
    suspend fun postDestinationsPersonalized(
        @Body body: AiDestinationRecommendationRequestBody,
    ): Response<ResponseBody>

    /** Suggests contextual activities based on trip cues and preferences. */
    @POST("recommendations/activities/contextual")
    suspend fun postActivitiesContextual(@Body body: AiContextualActivityRequestBody): Response<ResponseBody>

    /** Computes a mixed recommendation bundle for dashboard modules. */
    @POST("recommendations/personalized")
    suspend fun postPersonalizedRecommendations(@Body body: AiRecommendationRequestBody): Response<ResponseBody>

    /** Lists popular activities near a human-readable location label. */
    @GET("recommendations/popular/{location}")
    suspend fun getPopularActivities(
        @Path("location") location: String,
        @Query("limit") limit: Int = 10,
    ): Response<ResponseBody>

    /** Returns trending experiences with optional category filtering. */
    @GET("recommendations/trending")
    suspend fun getTrendingActivities(
        @Query("category") category: String? = null,
        @Query("limit") limit: Int = 10,
    ): Response<ResponseBody>

    /** Finds items similar to a reference activity identifier. */
    @GET("recommendations/similar/{activity_id}")
    suspend fun getSimilarActivities(
        @Path("activity_id") activityId: String,
        @Query("limit") limit: Int = 5,
    ): Response<ResponseBody>

    /** Records thumbs up/down style feedback to refine future rankings. */
    @POST("recommendations/feedback")
    suspend fun postRecommendationFeedback(
        @Query("user_id") userId: String,
        @Query("activity_id") activityId: String,
        @Query("rating") rating: Int,
        @Query("feedback_text") feedbackText: String? = null,
    ): Response<ResponseBody>

    /** Lists canonical activity category identifiers from the recommender. */
    @GET("recommendations/categories")
    suspend fun getActivityCategories(): Response<ResponseBody>

    // --- users (perfil IA) ---
    /** Bootstraps an AI-side user profile mirror. */
    @POST("users/profile")
    suspend fun postUserProfile(@Body body: AiUserProfileBody): Response<ResponseBody>

    /** Retrieves AI enrichment data for a user id. */
    @GET("users/profile/{user_id}")
    suspend fun getUserProfile(@Path("user_id") userId: String): Response<ResponseBody>

    /** Overwrites mutable AI profile fields. */
    @PUT("users/profile/{user_id}")
    suspend fun putUserProfile(
        @Path("user_id") userId: String,
        @Body body: AiUserProfileUpdateBody,
    ): Response<ResponseBody>

    /** Sends compact preference vectors learned from UI interactions. */
    @POST("users/preferences/{user_id}")
    suspend fun postUserPreferences(
        @Path("user_id") userId: String,
        @Body body: AiUserPreferencesBody,
    ): Response<ResponseBody>

    /** Notifies the service about a user gesture for modeling. */
    @POST("users/interaction")
    suspend fun postUserInteraction(@Body body: AiUserInteractionBody): Response<ResponseBody>

    /** Streams the latest ranked interactions for analytics views. */
    @GET("users/history/{user_id}")
    suspend fun getUserInteractionHistory(
        @Path("user_id") userId: String,
        @Query("limit") limit: Int = 50,
    ): Response<ResponseBody>

    /** Fetches condensed insight statements for coaching UI surfaces. */
    @GET("users/insights/{user_id}")
    suspend fun getUserInsights(@Path("user_id") userId: String): Response<ResponseBody>

    /** Deletes mirrored AI data for GDPR or account closure flows. */
    @DELETE("users/profile/{user_id}")
    suspend fun deleteUserProfile(@Path("user_id") userId: String): Response<ResponseBody>

    // --- matching ---
    /** Runs the AI traveler matching pipeline with rich filters. */
    @POST("matching/find")
    suspend fun postMatchingFind(@Body body: AiTravelerMatchRequestBody): Response<ResponseBody>

    /** Returns a compatibility score between two Voyager user ids. */
    @GET("matching/compatibility/{user_id}/{target_user_id}")
    suspend fun getMatchingCompatibility(
        @Path("user_id") userId: String,
        @Path("target_user_id") targetUserId: String,
    ): Response<ResponseBody>

    /** Initiates a match connection with an optional ice-breaker message. */
    @POST("matching/connect/{user_id}/{target_user_id}")
    suspend fun postMatchingConnect(
        @Path("user_id") userId: String,
        @Path("target_user_id") targetUserId: String,
        @Query("message") message: String? = null,
    ): Response<ResponseBody>

    /** Lists pending or accepted AI-suggested connections for review. */
    @GET("matching/connections/{user_id}")
    suspend fun getMatchingConnections(
        @Path("user_id") userId: String,
        @Query("status") status: String? = null,
    ): Response<ResponseBody>

    /** Accepts or declines an AI match suggestion by connection id. */
    @PUT("matching/connections/{connection_id}/respond")
    suspend fun putMatchingConnectionRespond(
        @Path("connection_id") connectionId: String,
        @Query("response") response: String,
        @Query("message") message: String? = null,
    ): Response<ResponseBody>

    /** Suggests additional buddies near an optional geo hint. */
    @GET("matching/recommendations/{user_id}")
    suspend fun getTravelBuddyRecommendations(
        @Path("user_id") userId: String,
        @Query("location") location: String? = null,
        @Query("limit") limit: Int = 10,
    ): Response<ResponseBody>

    /** Logs whether a suggestion converted to a real-world meetup. */
    @POST("matching/learning/connection-outcome")
    suspend fun postConnectionOutcome(@Body body: AiConnectionOutcomeRequestBody): Response<ResponseBody>

    /** Captures qualitative feedback about a proposed match pairing. */
    @POST("matching/feedback/{user_id}/{target_user_id}")
    suspend fun postMatchFeedback(
        @Path("user_id") userId: String,
        @Path("target_user_id") targetUserId: String,
        @Query("rating") rating: Int,
        @Query("feedback_text") feedbackText: String? = null,
    ): Response<ResponseBody>

    // --- trends ---
    /** Aggregated KPIs for executive trend dashboards. */
    @GET("trends/dashboard")
    suspend fun getTrendsDashboard(): Response<ResponseBody>

    /** Deep dive metrics for a named marketing segment. */
    @GET("trends/segments/{segment_id}/insights")
    suspend fun getSegmentInsights(@Path("segment_id") segmentId: String): Response<ResponseBody>

    /** Weekly narrative digest for newsletter or push notifications. */
    @GET("trends/weekly-digest")
    suspend fun getWeeklyDigest(): Response<ResponseBody>

    // --- seasonality ---
    /** Overview of seasonal demand curves with optional pivot month. */
    @GET("seasonality/overview")
    suspend fun getSeasonalityOverview(@Query("reference_month") referenceMonth: Int? = null): Response<ResponseBody>

    /** Forecast notes for a specific destination identifier. */
    @GET("seasonality/destinations/{destination_id}")
    suspend fun getDestinationSeasonalProfile(@Path("destination_id") destinationId: String): Response<ResponseBody>

    /** Runs a seasonal demand projection with structured inputs. */
    @POST("seasonality/forecast")
    suspend fun postSeasonalForecast(@Body body: AiSeasonalForecastRequestBody): Response<ResponseBody>

    /** Applies AI-proposed ranking boosts for shoulder seasons. */
    @POST("seasonality/visibility-adjustments")
    suspend fun postVisibilityAdjustments(@Body body: AiVisibilityAdjustmentsRequestBody): Response<ResponseBody>

    // --- chat ---
    /** Sends a user utterance to the guided travel assistant. */
    @POST("chat")
    suspend fun postChat(@Body body: AiChatRequestDto): Response<AiChatReplyDto>

    /** Retrieves recent chat turns for continuity in the UI. */
    @GET("chat/{user_id}/history")
    suspend fun getChatHistory(@Path("user_id") userId: String): Response<ResponseBody>

    /** Clears stored chat transcripts for privacy resets. */
    @DELETE("chat/{user_id}/history")
    suspend fun deleteChatHistory(@Path("user_id") userId: String): Response<ResponseBody>

    // --- adaptive UI ---
    /** Builds a role-aware menu tree for adaptive shells. */
    @GET("adaptive-ui/menu/{user_id}")
    suspend fun getAdaptiveMenu(@Path("user_id") userId: String): Response<ResponseBody>

    /** Home feed cards curated from AI ranking signals. */
    @GET("adaptive-ui/home-feed/{user_id}")
    suspend fun getAdaptiveHomeFeed(@Path("user_id") userId: String): Response<ResponseBody>

    // --- service diagnostics (absolute paths on host) ---
    /** Public landing JSON describing the FastAPI service. */
    @GET("/")
    suspend fun getServiceRoot(): Response<ResponseBody>

    /** Lightweight readiness probe for uptime monitors. */
    @GET("/health")
    suspend fun getHealth(): Response<ResponseBody>
}
