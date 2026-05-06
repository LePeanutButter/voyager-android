package com.voyager.tourism.data.api

import com.voyager.tourism.data.dto.ApiResponse
import com.voyager.tourism.data.dto.PagedResponseTravelPlanDto
import com.voyager.tourism.data.dto.ReservationDto
import com.voyager.tourism.data.dto.TravelPlanActivityDto
import com.voyager.tourism.data.dto.TravelPlanDto
import com.voyager.tourism.data.dto.TravelerMatchDto
import com.voyager.tourism.data.dto.CreateActivityRequest
import com.voyager.tourism.data.dto.UpdateActivityRequest
import com.voyager.tourism.data.dto.ConnectionDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Endpoints de [com.tourism.platform.controller.TravelPlanController].
 */
interface TravelPlanApiService {

    @POST("travel-plans")
    suspend fun createTravelPlan(@Body body: TravelPlanDto): ApiResponse<TravelPlanDto>

    @GET("travel-plans/{id}")
    suspend fun getTravelPlanById(@Path("id") id: Long): ApiResponse<TravelPlanDto>

    @GET("travel-plans")
    suspend fun getMyTravelPlans(): ApiResponse<List<TravelPlanDto>>

    @GET("travel-plans/user/{userId}")
    suspend fun getTravelPlansByUser(
        @Path("userId") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): PagedResponseTravelPlanDto

    @PUT("travel-plans/{id}")
    suspend fun updateTravelPlan(@Path("id") id: Long, @Body body: TravelPlanDto): ApiResponse<TravelPlanDto>

    @DELETE("travel-plans/{id}")
    suspend fun deleteTravelPlan(@Path("id") id: Long): ApiResponse<Unit>

    @POST("travel-plans/{id}/activities")
    suspend fun addActivity(
        @Path("id") planId: Long,
        @Body body: CreateActivityRequest,
    ): ApiResponse<TravelPlanActivityDto>

    @GET("travel-plans/{id}/activities")
    suspend fun getActivities(@Path("id") planId: Long): ApiResponse<List<TravelPlanActivityDto>>

    @PUT("travel-plans/{id}/activities/{activityId}")
    suspend fun updateActivity(
        @Path("id") planId: Long,
        @Path("activityId") activityId: Long,
        @Body body: UpdateActivityRequest,
    ): ApiResponse<TravelPlanActivityDto>

    @DELETE("travel-plans/{id}/activities/{activityId}")
    suspend fun deleteActivity(
        @Path("id") planId: Long,
        @Path("activityId") activityId: Long,
    ): ApiResponse<Unit>

    @GET("travel-plans/{id}/connections")
    suspend fun getTravelPlanConnections(
        @Path("id") planId: Long,
        @Query("status") status: String = "ACCEPTED",
    ): ApiResponse<List<ConnectionDto>>

    @POST("travel-plans/{id}/reservations")
    suspend fun addReservation(
        @Path("id") planId: Long,
        @Body body: ReservationDto,
    ): ApiResponse<ReservationDto>

    @GET("travel-plans/{id}/reservations")
    suspend fun getReservations(@Path("id") planId: Long): ApiResponse<List<ReservationDto>>

    @POST("travel-plans/{id}/share")
    suspend fun shareTravelPlan(@Path("id") planId: Long): ApiResponse<String>

    @GET("travel-plans/shared/{shareToken}")
    suspend fun getSharedTravelPlan(@Path("shareToken") shareToken: String): ApiResponse<TravelPlanDto>

    @GET("travel-plans/status/{status}")
    suspend fun getTravelPlansByStatus(
        @Path("status") status: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): PagedResponseTravelPlanDto

    @GET("travel-plans/type/{type}")
    suspend fun getTravelPlansByType(
        @Path("type") type: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): PagedResponseTravelPlanDto

    @PUT("travel-plans/{id}/status")
    suspend fun updateTravelPlanStatus(
        @Path("id") planId: Long,
        @Query("status") status: String,
    ): ApiResponse<TravelPlanDto>

    @GET("travel-plans/{id}/compatible-travelers")
    suspend fun findCompatibleTravelers(@Path("id") planId: Long): ApiResponse<List<TravelerMatchDto>>
}
