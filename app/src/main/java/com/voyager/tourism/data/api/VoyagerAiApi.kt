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
 * Resto del microservicio FastAPI bajo `/api/v1` (recomendaciones, usuarios IA, matching IA, tendencias, estacionalidad, chat).
 * Preferencias por cuestionario y behavior-analysis siguen en [AiTravelPreferencesApi] y [BehaviorAnalysisApi].
 * Muchas respuestas JSON dinámicas se exponen como [ResponseBody] para invocación fiable contra la nube.
 */
interface VoyagerAiApi {

    // --- recommendations ---
    @POST("recommendations/destinations/personalized")
    suspend fun postDestinationsPersonalized(
        @Body body: AiDestinationRecommendationRequestBody,
    ): Response<ResponseBody>

    @POST("recommendations/activities/contextual")
    suspend fun postActivitiesContextual(@Body body: AiContextualActivityRequestBody): Response<ResponseBody>

    @POST("recommendations/personalized")
    suspend fun postPersonalizedRecommendations(@Body body: AiRecommendationRequestBody): Response<ResponseBody>

    @GET("recommendations/popular/{location}")
    suspend fun getPopularActivities(
        @Path("location") location: String,
        @Query("limit") limit: Int = 10,
    ): Response<ResponseBody>

    @GET("recommendations/trending")
    suspend fun getTrendingActivities(
        @Query("category") category: String? = null,
        @Query("limit") limit: Int = 10,
    ): Response<ResponseBody>

    @GET("recommendations/similar/{activity_id}")
    suspend fun getSimilarActivities(
        @Path("activity_id") activityId: String,
        @Query("limit") limit: Int = 5,
    ): Response<ResponseBody>

    @POST("recommendations/feedback")
    suspend fun postRecommendationFeedback(
        @Query("user_id") userId: String,
        @Query("activity_id") activityId: String,
        @Query("rating") rating: Int,
        @Query("feedback_text") feedbackText: String? = null,
    ): Response<ResponseBody>

    @GET("recommendations/categories")
    suspend fun getActivityCategories(): Response<ResponseBody>

    // --- users (perfil IA) ---
    @POST("users/profile")
    suspend fun postUserProfile(@Body body: AiUserProfileBody): Response<ResponseBody>

    @GET("users/profile/{user_id}")
    suspend fun getUserProfile(@Path("user_id") userId: String): Response<ResponseBody>

    @PUT("users/profile/{user_id}")
    suspend fun putUserProfile(
        @Path("user_id") userId: String,
        @Body body: AiUserProfileUpdateBody,
    ): Response<ResponseBody>

    @POST("users/preferences/{user_id}")
    suspend fun postUserPreferences(
        @Path("user_id") userId: String,
        @Body body: AiUserPreferencesBody,
    ): Response<ResponseBody>

    @POST("users/interaction")
    suspend fun postUserInteraction(@Body body: AiUserInteractionBody): Response<ResponseBody>

    @GET("users/history/{user_id}")
    suspend fun getUserInteractionHistory(
        @Path("user_id") userId: String,
        @Query("limit") limit: Int = 50,
    ): Response<ResponseBody>

    @GET("users/insights/{user_id}")
    suspend fun getUserInsights(@Path("user_id") userId: String): Response<ResponseBody>

    @DELETE("users/profile/{user_id}")
    suspend fun deleteUserProfile(@Path("user_id") userId: String): Response<ResponseBody>

    // --- matching ---
    @POST("matching/find")
    suspend fun postMatchingFind(@Body body: AiTravelerMatchRequestBody): Response<ResponseBody>

    @GET("matching/compatibility/{user_id}/{target_user_id}")
    suspend fun getMatchingCompatibility(
        @Path("user_id") userId: String,
        @Path("target_user_id") targetUserId: String,
    ): Response<ResponseBody>

    @POST("matching/connect/{user_id}/{target_user_id}")
    suspend fun postMatchingConnect(
        @Path("user_id") userId: String,
        @Path("target_user_id") targetUserId: String,
        @Query("message") message: String? = null,
    ): Response<ResponseBody>

    @GET("matching/connections/{user_id}")
    suspend fun getMatchingConnections(
        @Path("user_id") userId: String,
        @Query("status") status: String? = null,
    ): Response<ResponseBody>

    @PUT("matching/connections/{connection_id}/respond")
    suspend fun putMatchingConnectionRespond(
        @Path("connection_id") connectionId: String,
        @Query("response") response: String,
        @Query("message") message: String? = null,
    ): Response<ResponseBody>

    @GET("matching/recommendations/{user_id}")
    suspend fun getTravelBuddyRecommendations(
        @Path("user_id") userId: String,
        @Query("location") location: String? = null,
        @Query("limit") limit: Int = 10,
    ): Response<ResponseBody>

    @POST("matching/learning/connection-outcome")
    suspend fun postConnectionOutcome(@Body body: AiConnectionOutcomeRequestBody): Response<ResponseBody>

    @POST("matching/feedback/{user_id}/{target_user_id}")
    suspend fun postMatchFeedback(
        @Path("user_id") userId: String,
        @Path("target_user_id") targetUserId: String,
        @Query("rating") rating: Int,
        @Query("feedback_text") feedbackText: String? = null,
    ): Response<ResponseBody>

    // --- trends ---
    @GET("trends/dashboard")
    suspend fun getTrendsDashboard(): Response<ResponseBody>

    @GET("trends/segments/{segment_id}/insights")
    suspend fun getSegmentInsights(@Path("segment_id") segmentId: String): Response<ResponseBody>

    @GET("trends/weekly-digest")
    suspend fun getWeeklyDigest(): Response<ResponseBody>

    // --- seasonality ---
    @GET("seasonality/overview")
    suspend fun getSeasonalityOverview(@Query("reference_month") referenceMonth: Int? = null): Response<ResponseBody>

    @GET("seasonality/destinations/{destination_id}")
    suspend fun getDestinationSeasonalProfile(@Path("destination_id") destinationId: String): Response<ResponseBody>

    @POST("seasonality/forecast")
    suspend fun postSeasonalForecast(@Body body: AiSeasonalForecastRequestBody): Response<ResponseBody>

    @POST("seasonality/visibility-adjustments")
    suspend fun postVisibilityAdjustments(@Body body: AiVisibilityAdjustmentsRequestBody): Response<ResponseBody>

    // --- chat ---
    @POST("chat")
    suspend fun postChat(@Body body: AiChatRequestDto): Response<AiChatReplyDto>

    @GET("chat/{user_id}/history")
    suspend fun getChatHistory(@Path("user_id") userId: String): Response<ResponseBody>

    @DELETE("chat/{user_id}/history")
    suspend fun deleteChatHistory(@Path("user_id") userId: String): Response<ResponseBody>

    // --- adaptive UI ---
    @GET("adaptive-ui/menu/{user_id}")
    suspend fun getAdaptiveMenu(@Path("user_id") userId: String): Response<ResponseBody>

    @GET("adaptive-ui/home-feed/{user_id}")
    suspend fun getAdaptiveHomeFeed(@Path("user_id") userId: String): Response<ResponseBody>

    // --- diagnóstico raíz del servicio (ruta absoluta respecto al host) ---
    @GET("/")
    suspend fun getServiceRoot(): Response<ResponseBody>

    @GET("/health")
    suspend fun getHealth(): Response<ResponseBody>
}
