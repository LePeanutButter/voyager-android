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
 * Compatibilidad, matching global, compartir actividades y ruta legacy.
 */
interface BackendMiscApiService {

    @POST("compatibility/matches")
    suspend fun findCompatibilityMatches(
        @Body body: CompatibilityMatchRequestDto,
    ): ApiResponse<List<CompatibilityMatchResponseDto>>

    @GET("matches")
    suspend fun getDestinationMatches(
        @Query("destination") destination: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("interests") interests: List<String>? = null,
        @Query("limit") limit: Int = 20,
    ): ApiResponse<List<MatchResponseDto>>

    @POST("activities/{activityId}/share")
    suspend fun shareActivity(
        @Path("activityId") activityId: Long,
        @Body body: ShareActivityRequestDto,
    ): ApiResponse<SharedActivityResponseDto>

    @PATCH("shared-activities/{sharedActivityId}")
    suspend fun updateSharedActivity(
        @Path("sharedActivityId") sharedActivityId: Long,
        @Body body: SharedActivityActionRequestDto,
    ): ApiResponse<SharedActivityResponseDto>

    @POST("legacy/activities/{activityId}/share")
    suspend fun legacyShareActivity(
        @Path("activityId") activityId: Long,
        @Body body: ShareActivityRequestDto,
    ): ApiResponse<SharedActivityResponseDto>

    @PATCH("legacy/shared-activities/{sharedActivityId}")
    suspend fun legacyUpdateSharedActivity(
        @Path("sharedActivityId") sharedActivityId: Long,
        @Body body: SharedActivityActionRequestDto,
    ): ApiResponse<SharedActivityResponseDto>
}
