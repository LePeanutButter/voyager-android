package com.voyager.tourism.data.api

import com.voyager.tourism.data.dto.LoginRequest
import com.voyager.tourism.data.dto.LoginResponse
import com.voyager.tourism.data.dto.RegisterRequest
import com.voyager.tourism.data.dto.UserDto
import com.voyager.tourism.data.dto.TripDto
import com.voyager.tourism.data.dto.TravelerMatchDto
import com.voyager.tourism.data.dto.ConnectionRequestDto
import com.voyager.tourism.data.dto.SendConnectionRequestDto
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API service interface for Tourism Intelligent Platform
 * Defines all REST API endpoints for communication with backend
 */
interface TourismApiService {
    
    // Authentication endpoints
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<UserDto>
    
    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<Unit>
    
    @GET("auth/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<UserDto>
    
    // User endpoints
    @PUT("users/{userId}")
    suspend fun updateUser(
        @Path("userId") userId: String,
        @Body user: UserDto,
        @Header("Authorization") token: String
    ): Response<UserDto>
    
    @PUT("users/{userId}/preferences")
    suspend fun updateUserPreferences(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<UserDto>
    
    // Trip endpoints
    @GET("trips")
    suspend fun getUserTrips(
        @Query("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<List<TripDto>>
    
    @GET("trips/{tripId}")
    suspend fun getTripById(
        @Path("tripId") tripId: String,
        @Header("Authorization") token: String
    ): Response<TripDto>
    
    @POST("trips")
    suspend fun createTrip(
        @Body trip: TripDto,
        @Header("Authorization") token: String
    ): Response<TripDto>
    
    @PUT("trips/{tripId}")
    suspend fun updateTrip(
        @Path("tripId") tripId: String,
        @Body trip: TripDto,
        @Header("Authorization") token: String
    ): Response<TripDto>
    
    @DELETE("trips/{tripId}")
    suspend fun deleteTrip(
        @Path("tripId") tripId: String,
        @Header("Authorization") token: String
    ): Response<Unit>
    
    // Destination endpoints
    @GET("destinations/search")
    suspend fun searchDestinations(
        @Query("query") query: String,
        @Header("Authorization") token: String
    ): Response<List<com.voyager.tourism.data.dto.DestinationDto>>
    
    @GET("destinations/{destinationId}")
    suspend fun getDestinationById(
        @Path("destinationId") destinationId: String,
        @Header("Authorization") token: String
    ): Response<com.voyager.tourism.data.dto.DestinationDto>
    
    // Recommendations endpoints
    @GET("recommendations/destinations")
    suspend fun getDestinationRecommendations(
        @Query("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<List<com.voyager.tourism.data.dto.DestinationDto>>
    
    @GET("recommendations/activities")
    suspend fun getActivityRecommendations(
        @Query("userId") userId: String,
        @Query("destinationId") destinationId: String,
        @Header("Authorization") token: String
    ): Response<List<com.voyager.tourism.data.dto.ActivityDto>>
    
    // AI Assistant endpoints
    @POST("ai/chat")
    suspend fun sendAiMessage(
        @Body message: com.voyager.tourism.data.dto.AiChatRequest,
        @Header("Authorization") token: String
    ): Response<com.voyager.tourism.data.dto.AiChatResponse>
    
    // Social endpoints
    @GET("social/travelers")
    suspend fun getNearbyTravelers(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Header("Authorization") token: String
    ): Response<List<UserDto>>
    
    @POST("social/connect/{userId}")
    suspend fun connectWithTraveler(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<Unit>
    
    // Traveler matching endpoints
    @GET("travel-plans/{travelPlanId}/compatible-travelers")
    suspend fun getCompatibleTravelers(
        @Path("travelPlanId") travelPlanId: String,
        @Header("Authorization") token: String
    ): Response<List<TravelerMatchDto>>
    
    // Connection request endpoints
    @POST("social/connections")
    suspend fun sendConnectionRequest(
        @Body request: SendConnectionRequestDto,
        @Header("Authorization") token: String
    ): Response<ConnectionRequestDto>
    
    @PUT("social/connections/{requestId}/accept")
    suspend fun acceptConnectionRequest(
        @Path("requestId") requestId: String,
        @Header("Authorization") token: String
    ): Response<ConnectionRequestDto>
    
    @PUT("social/connections/{requestId}/reject")
    suspend fun rejectConnectionRequest(
        @Path("requestId") requestId: String,
        @Header("Authorization") token: String
    ): Response<ConnectionRequestDto>
    
    @GET("social/connections/pending")
    suspend fun getPendingRequests(
        @Header("Authorization") token: String
    ): Response<List<ConnectionRequestDto>>
    
    @GET("social/connections/sent")
    suspend fun getSentRequests(
        @Header("Authorization") token: String
    ): Response<List<ConnectionRequestDto>>
}
