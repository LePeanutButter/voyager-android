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
 * Retrofit contract for [com.tourism.platform.controller.TravelPlanController] travel-plan CRUD and related operations.
 */
interface TravelPlanApiService {

    /**
     * Creates a travel plan from a fully populated DTO payload.
     */
    @POST("travel-plans")
    suspend fun createTravelPlan(@Body body: TravelPlanDto): ApiResponse<TravelPlanDto>

    /**
     * Loads one travel plan by id.
     */
    @GET("travel-plans/{id}")
    suspend fun getTravelPlanById(@Path("id") id: Long): ApiResponse<TravelPlanDto>

    /**
     * Returns travel plans for the authenticated session user.
     */
    @GET("travel-plans")
    suspend fun getMyTravelPlans(): ApiResponse<List<TravelPlanDto>>

    /**
     * Lists travel plans for a specific user with paging.
     */
    @GET("travel-plans/user/{userId}")
    suspend fun getTravelPlansByUser(
        @Path("userId") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): PagedResponseTravelPlanDto

    /**
     * Updates mutable plan fields in place.
     */
    @PUT("travel-plans/{id}")
    suspend fun updateTravelPlan(@Path("id") id: Long, @Body body: TravelPlanDto): ApiResponse<TravelPlanDto>

    /**
     * Deletes a plan permanently.
     */
    @DELETE("travel-plans/{id}")
    suspend fun deleteTravelPlan(@Path("id") id: Long): ApiResponse<Unit>

    /**
     * Appends a new itinerary activity under a plan.
     */
    @POST("travel-plans/{id}/activities")
    suspend fun addActivity(
        @Path("id") planId: Long,
        @Body body: CreateActivityRequest,
    ): ApiResponse<TravelPlanActivityDto>

    /**
     * Lists all activities attached to a plan.
     */
    @GET("travel-plans/{id}/activities")
    suspend fun getActivities(@Path("id") planId: Long): ApiResponse<List<TravelPlanActivityDto>>

    /**
     * Updates an existing activity line item.
     */
    @PUT("travel-plans/{id}/activities/{activityId}")
    suspend fun updateActivity(
        @Path("id") planId: Long,
        @Path("activityId") activityId: Long,
        @Body body: UpdateActivityRequest,
    ): ApiResponse<TravelPlanActivityDto>

    /**
     * Removes a single activity from the plan timeline.
     */
    @DELETE("travel-plans/{id}/activities/{activityId}")
    suspend fun deleteActivity(
        @Path("id") planId: Long,
        @Path("activityId") activityId: Long,
    ): ApiResponse<Unit>

    /**
     * Lists social connections tied to the plan with optional status filter.
     */
    @GET("travel-plans/{id}/connections")
    suspend fun getTravelPlanConnections(
        @Path("id") planId: Long,
        @Query("status") status: String = "ACCEPTED",
    ): ApiResponse<List<ConnectionDto>>

    /**
     * Adds or confirms a reservation record under the plan.
     */
    @POST("travel-plans/{id}/reservations")
    suspend fun addReservation(
        @Path("id") planId: Long,
        @Body body: ReservationDto,
    ): ApiResponse<ReservationDto>

    /**
     * Lists all reservations registered for the plan.
     */
    @GET("travel-plans/{id}/reservations")
    suspend fun getReservations(@Path("id") planId: Long): ApiResponse<List<ReservationDto>>

    /**
     * Creates a public share link token for read-only viewing.
     */
    @POST("travel-plans/{id}/share")
    suspend fun shareTravelPlan(@Path("id") planId: Long): ApiResponse<String>

    /**
     * Opens a plan that was shared via opaque token.
     */
    @GET("travel-plans/shared/{shareToken}")
    suspend fun getSharedTravelPlan(@Path("shareToken") shareToken: String): ApiResponse<TravelPlanDto>

    /**
     * Filters paginated plans by canonical status enum.
     */
    @GET("travel-plans/status/{status}")
    suspend fun getTravelPlansByStatus(
        @Path("status") status: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): PagedResponseTravelPlanDto

    /**
     * Filters paginated plans by travel type bucket.
     */
    @GET("travel-plans/type/{type}")
    suspend fun getTravelPlansByType(
        @Path("type") type: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): PagedResponseTravelPlanDto

    /**
     * Updates only the status field without replacing the entire DTO.
     */
    @PUT("travel-plans/{id}/status")
    suspend fun updateTravelPlanStatus(
        @Path("id") planId: Long,
        @Query("status") status: String,
    ): ApiResponse<TravelPlanDto>

    /**
     * Returns candidate travelers compatible with the plan constraints.
     */
    @GET("travel-plans/{id}/compatible-travelers")
    suspend fun findCompatibleTravelers(@Path("id") planId: Long): ApiResponse<List<TravelerMatchDto>>
}
