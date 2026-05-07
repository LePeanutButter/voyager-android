package com.voyager.tourism.data.api

import com.voyager.tourism.data.dto.ApiResponse
import com.voyager.tourism.data.dto.CompatibilityMatchRequestDto
import com.voyager.tourism.data.dto.CompatibilityMatchResponseDto
import com.voyager.tourism.data.dto.MatchResponseDto
import com.voyager.tourism.data.dto.SharedActivityActionRequestDto
import com.voyager.tourism.data.dto.SharedActivityResponseDto
import com.voyager.tourism.data.dto.ShareActivityRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Miscellaneous backend endpoints for compatibility matching, global destination matching,
 * shared activities, and legacy sharing routes.
 */
interface BackendMiscApiService {

    /**
     * Finds traveler compatibility matches from a structured request payload.
     */
    @POST("compatibility/matches")
    suspend fun findCompatibilityMatches(
        @Body body: CompatibilityMatchRequestDto,
    ): ApiResponse<List<CompatibilityMatchResponseDto>>

    /**
     * Returns destination-based matches for a date range and optional interest filters.
     */
    @GET("matches")
    suspend fun getDestinationMatches(
        @Query("destination") destination: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("interests") interests: List<String>? = null,
        @Query("limit") limit: Int = 20,
    ): ApiResponse<List<MatchResponseDto>>

    /**
     * Shares an activity with a recipient user, creating or updating a shared activity row.
     */
    @POST("activities/{activityId}/share")
    suspend fun shareActivity(
        @Path("activityId") activityId: Long,
        @Body body: ShareActivityRequestDto,
    ): ApiResponse<SharedActivityResponseDto>

    /**
     * Applies an action (accept/decline/etc.) to a shared activity instance.
     */
    @PATCH("shared-activities/{sharedActivityId}")
    suspend fun updateSharedActivity(
        @Path("sharedActivityId") sharedActivityId: Long,
        @Body body: SharedActivityActionRequestDto,
    ): ApiResponse<SharedActivityResponseDto>

    /**
     * Legacy route that mirrors [shareActivity] for older clients.
     */
    @POST("legacy/activities/{activityId}/share")
    suspend fun legacyShareActivity(
        @Path("activityId") activityId: Long,
        @Body body: ShareActivityRequestDto,
    ): ApiResponse<SharedActivityResponseDto>

    /**
     * Legacy route that mirrors [updateSharedActivity] for older clients.
     */
    @PATCH("legacy/shared-activities/{sharedActivityId}")
    suspend fun legacyUpdateSharedActivity(
        @Path("sharedActivityId") sharedActivityId: Long,
        @Body body: SharedActivityActionRequestDto,
    ): ApiResponse<SharedActivityResponseDto>
}
