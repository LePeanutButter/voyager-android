package com.voyager.tourism.data.api

import com.voyager.tourism.data.dto.ApiResponse
import com.voyager.tourism.data.dto.ConnectionDto
import com.voyager.tourism.data.dto.ConnectionRequestDto
import com.voyager.tourism.data.dto.MessageDto
import com.voyager.tourism.data.dto.PagedResponseMessageDto
import com.voyager.tourism.data.dto.SendConnectionRequestDto
import com.voyager.tourism.data.dto.SendMessageRequestDto
import com.voyager.tourism.data.dto.TravelerSummaryDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.ResponseBody

/**
 * Endpoints de [com.tourism.platform.controller.SocialController].
 * Respuestas con `Map` genérico del backend se exponen como [ResponseBody] para parseo flexible.
 */
interface SocialApiService {

    @POST("social/connections")
    suspend fun sendConnectionRequest(@Body body: SendConnectionRequestDto): ApiResponse<ConnectionRequestDto>

    @GET("social/connections/{userId}")
    suspend fun getUserConnections(@Path("userId") userId: Long): ApiResponse<List<ConnectionDto>>

    @PUT("social/connections/{requestId}/accept")
    suspend fun acceptConnectionRequest(@Path("requestId") requestId: Long): ApiResponse<ConnectionRequestDto>

    @PUT("social/connections/{requestId}/reject")
    suspend fun rejectConnectionRequest(@Path("requestId") requestId: Long): ApiResponse<ConnectionRequestDto>

    @GET("social/connections/pending")
    suspend fun getPendingRequests(): ApiResponse<List<ConnectionRequestDto>>

    @GET("social/connections/sent")
    suspend fun getSentRequests(): ApiResponse<List<ConnectionRequestDto>>

    @DELETE("social/connections/{connectionId}")
    suspend fun removeConnection(@Path("connectionId") connectionId: Long): ApiResponse<Unit>

    @GET("social/travelers/{id}/summary")
    suspend fun getTravelerSummary(@Path("id") travelerId: Long): ApiResponse<TravelerSummaryDto>

    @POST("social/reviews")
    suspend fun createReview(@Body body: Map<String, @JvmSuppressWildcards Any?>): ApiResponse<Map<String, @JvmSuppressWildcards Any?>>

    @GET("social/reviews/{targetType}/{targetId}")
    suspend fun getReviews(
        @Path("targetType") targetType: String,
        @Path("targetId") targetId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): Response<ResponseBody>

    @PUT("social/reviews/{reviewId}")
    suspend fun updateReview(
        @Path("reviewId") reviewId: Long,
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): ApiResponse<Map<String, @JvmSuppressWildcards Any?>>

    @DELETE("social/reviews/{reviewId}")
    suspend fun deleteReview(@Path("reviewId") reviewId: Long): ApiResponse<Unit>

    @GET("social/conversations/{userId}")
    suspend fun getConversations(@Path("userId") userId: Long): Response<ResponseBody>

    @GET("social/connections/{connectionId}/messages")
    suspend fun getConversationMessages(
        @Path("connectionId") connectionId: Long,
        @Query("userId") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
    ): PagedResponseMessageDto

    @PUT("social/messages/{messageId}/read")
    suspend fun markMessageAsRead(@Path("messageId") messageId: Long): ApiResponse<Unit>

    @GET("social/feed/{userId}")
    suspend fun getSocialFeed(
        @Path("userId") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): Response<ResponseBody>

    @POST("social/posts")
    suspend fun createPost(@Body body: Map<String, @JvmSuppressWildcards Any?>): ApiResponse<Map<String, @JvmSuppressWildcards Any?>>

    @POST("social/posts/{postId}/like")
    suspend fun likePost(@Path("postId") postId: Long): ApiResponse<Map<String, @JvmSuppressWildcards Any?>>

    @DELETE("social/posts/{postId}/like")
    suspend fun unlikePost(@Path("postId") postId: Long): ApiResponse<Map<String, @JvmSuppressWildcards Any?>>

    @POST("social/posts/{postId}/comments")
    suspend fun commentOnPost(
        @Path("postId") postId: Long,
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): ApiResponse<Map<String, @JvmSuppressWildcards Any?>>

    @GET("social/posts/{postId}/comments")
    suspend fun getPostComments(
        @Path("postId") postId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): Response<ResponseBody>

    @POST("social/messages")
    suspend fun sendMessage(@Body body: SendMessageRequestDto): ApiResponse<MessageDto>
}
