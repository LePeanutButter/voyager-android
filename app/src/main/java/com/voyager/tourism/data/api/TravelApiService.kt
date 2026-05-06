package com.voyager.tourism.data.api

import com.voyager.tourism.data.dto.ApiResponse
import com.voyager.tourism.data.dto.TravelPlanDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Practical façade over travel-plan endpoints for modules still depending on [TravelApiService].
 * Exposes the subset used by [com.voyager.tourism.data.repository.TravelRepositoryImpl], mirroring [TravelPlanApiService].
 */
interface TravelApiService {

    /**
     * Creates a new travel plan on the backend.
     */
    @POST("travel-plans")
    suspend fun createTravelPlan(@Body body: TravelPlanDto): ApiResponse<TravelPlanDto>

    /**
     * Returns a paged list of travel plans owned by the given user.
     */
    @GET("travel-plans/user/{userId}")
    suspend fun getUserTravelPlans(@Path("userId") userId: Long): com.voyager.tourism.data.dto.PagedResponseTravelPlanDto

    /**
     * Loads a single travel plan by numeric identifier.
     */
    @GET("travel-plans/{id}")
    suspend fun getTravelPlanById(@Path("id") id: Long): ApiResponse<TravelPlanDto>

    /**
     * Replaces fields on an existing travel plan.
     */
    @PUT("travel-plans/{id}")
    suspend fun updateTravelPlan(@Path("id") id: Long, @Body body: TravelPlanDto): ApiResponse<TravelPlanDto>

    /**
     * Deletes a travel plan permanently.
     */
    @DELETE("travel-plans/{id}")
    suspend fun deleteTravelPlan(@Path("id") id: Long): ApiResponse<Unit>
}
