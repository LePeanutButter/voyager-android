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
 * Alias práctico de planes de viaje para capas que aún usan [TravelApiService].
 * Misma API que [TravelPlanApiService] (subset usado por [TravelRepositoryImpl]).
 */
interface TravelApiService {

    @POST("travel-plans")
    suspend fun createTravelPlan(@Body body: TravelPlanDto): ApiResponse<TravelPlanDto>

    @GET("travel-plans/user/{userId}")
    suspend fun getUserTravelPlans(@Path("userId") userId: Long): com.voyager.tourism.data.dto.PagedResponseTravelPlanDto

    @GET("travel-plans/{id}")
    suspend fun getTravelPlanById(@Path("id") id: Long): ApiResponse<TravelPlanDto>

    @PUT("travel-plans/{id}")
    suspend fun updateTravelPlan(@Path("id") id: Long, @Body body: TravelPlanDto): ApiResponse<TravelPlanDto>

    @DELETE("travel-plans/{id}")
    suspend fun deleteTravelPlan(@Path("id") id: Long): ApiResponse<Unit>
}
