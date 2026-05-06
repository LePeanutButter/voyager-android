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
 * Retrofit contract for [com.tourism.platform.controller.SocialController].
 * Endpoints that return loosely typed JSON maps use [ResponseBody] or maps for flexible parsing on the client.
 */
interface SocialApiService {

    /**
     * Sends a social connection request between two users.
     */
    @POST("social/connections")
    suspend fun sendConnectionRequest(@Body body: SendConnectionRequestDto): ApiResponse<ConnectionRequestDto>

    /**
     * Lists a user's accepted connections as DTOs.
     */
    @GET("social/connections/{userId}")
    suspend fun getUserConnections(@Path("userId") userId: Long): ApiResponse<List<ConnectionDto>>

    /**
     * Accepts a previously pending connection request.
     */
    @PUT("social/connections/{requestId}/accept")
    suspend fun acceptConnectionRequest(@Path("requestId") requestId: Long): ApiResponse<ConnectionRequestDto>

    /**
     * Rejects a pending connection request.
     */
    @PUT("social/connections/{requestId}/reject")
    suspend fun rejectConnectionRequest(@Path("requestId") requestId: Long): ApiResponse<ConnectionRequestDto>

    /**
     * Lists inbound requests awaiting a decision.
     */
    @GET("social/connections/pending")
    suspend fun getPendingRequests(): ApiResponse<List<ConnectionRequestDto>>

    /**
     * Lists outbound requests that have not yet been answered.
     */
    @GET("social/connections/sent")
    suspend fun getSentRequests(): ApiResponse<List<ConnectionRequestDto>>

    /**
     * Removes an established connection edge.
     */
    @DELETE("social/connections/{connectionId}")
    suspend fun removeConnection(@Path("connectionId") connectionId: Long): ApiResponse<Unit>

    /**
     * Returns compact traveler profile data for cards and sheets.
     */
    @GET("social/travelers/{id}/summary")
    suspend fun getTravelerSummary(@Path("id") travelerId: Long): ApiResponse<TravelerSummaryDto>

    /**
     * Creates a social review payload using a loosely typed map to mirror backend fields.
     */
    @POST("social/reviews")
    suspend fun createReview(@Body body: Map<String, @JvmSuppressWildcards Any?>): ApiResponse<Map<String, @JvmSuppressWildcards Any?>>

    /**
     * Fetches paginated reviews for a subject (hotel, activity, etc.) as raw JSON.
     */
    @GET("social/reviews/{targetType}/{targetId}")
    suspend fun getReviews(
        @Path("targetType") targetType: String,
        @Path("targetId") targetId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): Response<ResponseBody>

    /**
     * Updates an existing review authored by the caller.
     */
    @PUT("social/reviews/{reviewId}")
    suspend fun updateReview(
        @Path("reviewId") reviewId: Long,
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): ApiResponse<Map<String, @JvmSuppressWildcards Any?>>

    /**
     * Deletes a review by id.
     */
    @DELETE("social/reviews/{reviewId}")
    suspend fun deleteReview(@Path("reviewId") reviewId: Long): ApiResponse<Unit>

    /**
     * Lists conversation metadata rows for a user as raw JSON.
     */
    @GET("social/conversations/{userId}")
    suspend fun getConversations(@Path("userId") userId: Long): Response<ResponseBody>

    /**
     * Returns a paged chat transcript between the authenticated user and a connection.
     */
    @GET("social/connections/{connectionId}/messages")
    suspend fun getConversationMessages(
        @Path("connectionId") connectionId: Long,
        @Query("userId") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
    ): PagedResponseMessageDto

    /**
     * Marks a direct message as read for unread counters.
     */
    @PUT("social/messages/{messageId}/read")
    suspend fun markMessageAsRead(@Path("messageId") messageId: Long): ApiResponse<Unit>

    /**
     * Streams social feed cards for the timeline UI as raw JSON.
     */
    @GET("social/feed/{userId}")
    suspend fun getSocialFeed(
        @Path("userId") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): Response<ResponseBody>

    /**
     * Publishes a new feed post with arbitrary backend fields.
     */
    @POST("social/posts")
    suspend fun createPost(@Body body: Map<String, @JvmSuppressWildcards Any?>): ApiResponse<Map<String, @JvmSuppressWildcards Any?>>

    /**
     * Records a like interaction on a feed post.
     */
    @POST("social/posts/{postId}/like")
    suspend fun likePost(@Path("postId") postId: Long): ApiResponse<Map<String, @JvmSuppressWildcards Any?>>

    /**
     * Removes a previously recorded like.
     */
    @DELETE("social/posts/{postId}/like")
    suspend fun unlikePost(@Path("postId") postId: Long): ApiResponse<Map<String, @JvmSuppressWildcards Any?>>

    /**
     * Adds a comment thread entry under a post.
     */
    @POST("social/posts/{postId}/comments")
    suspend fun commentOnPost(
        @Path("postId") postId: Long,
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): ApiResponse<Map<String, @JvmSuppressWildcards Any?>>

    /**
     * Lists comments for a given post as raw feed JSON.
     */
    @GET("social/posts/{postId}/comments")
    suspend fun getPostComments(
        @Path("postId") postId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): Response<ResponseBody>

    /**
     * Sends a chat message through the social graph.
     */
    @POST("social/messages")
    suspend fun sendMessage(@Body body: SendMessageRequestDto): ApiResponse<MessageDto>
}
