package com.voyager.tourism.domain.repository

import com.voyager.tourism.data.dto.ApiResponse
import com.voyager.tourism.data.dto.ConnectionDto
import com.voyager.tourism.data.dto.CreateActivityRequest
import com.voyager.tourism.data.dto.MatchResponseDto
import com.voyager.tourism.data.dto.MessageDto
import com.voyager.tourism.data.dto.PagedResponseMessageDto
import com.voyager.tourism.data.dto.PagedResponseTravelPlanDto
import com.voyager.tourism.data.dto.PagedResponseUserDto
import com.voyager.tourism.data.dto.ReservationDto
import com.voyager.tourism.data.dto.ShareActivityRequestDto
import com.voyager.tourism.data.dto.SharedActivityActionRequestDto
import com.voyager.tourism.data.dto.SharedActivityResponseDto
import com.voyager.tourism.data.dto.TravelPlanActivityDto
import com.voyager.tourism.data.dto.TravelPlanDto
import com.voyager.tourism.data.dto.TravelerSummaryDto
import com.voyager.tourism.data.dto.UpdateActivityRequest
import com.voyager.tourism.data.dto.UserDto
import com.voyager.tourism.data.dto.UserStatisticsDto
import com.voyager.tourism.data.dto.UserRegistrationDto
import com.voyager.tourism.data.dto.SendMessageRequestDto
import okhttp3.ResponseBody
import retrofit2.Response

/**
 * Endpoints del **voyager-backend-core** que no están cubiertos por
 * [UserRepository], [TripRepository], [SocialRepository], [AuthRepository] ni [TravelRepositoryImpl].
 * Permite invocar el resto de la API Spring desde casos de uso sin añadir lógica aquí.
 */
@Suppress("TooManyFunctions")
interface BackendSupplementRepository {

    // --- UserApiService (suplemento) ---
    suspend fun registerUserAlias(body: UserRegistrationDto): ApiResponse<UserDto>
    suspend fun getUserByUsername(username: String): ApiResponse<UserDto>
    suspend fun getUserByEmail(email: String): ApiResponse<UserDto>
    suspend fun changePassword(id: Long, currentPassword: String, newPassword: String): ApiResponse<Unit>
    suspend fun getAllUsers(
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "createdAt",
        sortDir: String = "desc",
    ): PagedResponseUserDto

    suspend fun getUsersByRole(role: String, page: Int = 0, size: Int = 20): PagedResponseUserDto
    suspend fun getUsersByStatus(status: String, page: Int = 0, size: Int = 20): PagedResponseUserDto
    suspend fun searchUsersByName(searchTerm: String, page: Int = 0, size: Int = 20): PagedResponseUserDto
    suspend fun updateUserRole(id: Long, role: String): ApiResponse<UserDto>
    suspend fun updateUserStatus(id: Long, status: String): ApiResponse<UserDto>
    suspend fun getUserStatistics(): ApiResponse<UserStatisticsDto>
    suspend fun checkUsernameAvailability(username: String): ApiResponse<Boolean>
    suspend fun checkEmailAvailability(email: String): ApiResponse<Boolean>

    // --- TravelPlanApiService (suplemento) ---
    suspend fun addActivity(planId: Long, body: CreateActivityRequest): ApiResponse<TravelPlanActivityDto>
    suspend fun updateActivity(
        planId: Long,
        activityId: Long,
        body: UpdateActivityRequest,
    ): ApiResponse<TravelPlanActivityDto>

    suspend fun deleteActivity(planId: Long, activityId: Long): ApiResponse<Unit>
    suspend fun getTravelPlanConnections(planId: Long, status: String = "ACCEPTED"): ApiResponse<List<ConnectionDto>>
    suspend fun addReservation(planId: Long, body: ReservationDto): ApiResponse<ReservationDto>
    suspend fun getReservations(planId: Long): ApiResponse<List<ReservationDto>>
    suspend fun shareTravelPlan(planId: Long): ApiResponse<String>
    suspend fun getSharedTravelPlan(shareToken: String): ApiResponse<TravelPlanDto>
    suspend fun getTravelPlansByStatus(status: String, page: Int = 0, size: Int = 20): PagedResponseTravelPlanDto
    suspend fun getTravelPlansByType(type: String, page: Int = 0, size: Int = 20): PagedResponseTravelPlanDto
    suspend fun updateTravelPlanStatus(planId: Long, status: String): ApiResponse<TravelPlanDto>

    // --- SocialApiService (suplemento) ---
    suspend fun removeConnection(connectionId: Long): ApiResponse<Unit>
    suspend fun getTravelerSummary(travelerId: Long): ApiResponse<TravelerSummaryDto>
    suspend fun createReview(body: Map<String, Any?>): ApiResponse<Map<String, Any?>>
    suspend fun getReviews(targetType: String, targetId: Long, page: Int = 0, size: Int = 20): Response<ResponseBody>
    suspend fun updateReview(reviewId: Long, body: Map<String, Any?>): ApiResponse<Map<String, Any?>>
    suspend fun deleteReview(reviewId: Long): ApiResponse<Unit>
    suspend fun getConversations(userId: Long): Response<ResponseBody>
    suspend fun getConversationMessages(
        connectionId: Long,
        userId: Long,
        page: Int = 0,
        size: Int = 50,
    ): PagedResponseMessageDto

    suspend fun markMessageAsRead(messageId: Long): ApiResponse<Unit>
    suspend fun getSocialFeed(userId: Long, page: Int = 0, size: Int = 20): Response<ResponseBody>
    suspend fun createPost(body: Map<String, Any?>): ApiResponse<Map<String, Any?>>
    suspend fun likePost(postId: Long): ApiResponse<Map<String, Any?>>
    suspend fun unlikePost(postId: Long): ApiResponse<Map<String, Any?>>
    suspend fun commentOnPost(postId: Long, body: Map<String, Any?>): ApiResponse<Map<String, Any?>>
    suspend fun getPostComments(postId: Long, page: Int = 0, size: Int = 20): Response<ResponseBody>
    suspend fun sendMessage(body: SendMessageRequestDto): ApiResponse<MessageDto>

    // --- BackendMiscApiService (suplemento) ---
    suspend fun getDestinationMatches(
        destination: String,
        startDate: String,
        endDate: String,
        interests: List<String>? = null,
        limit: Int = 20,
    ): ApiResponse<List<MatchResponseDto>>

    suspend fun legacyShareActivity(activityId: Long, body: ShareActivityRequestDto): ApiResponse<SharedActivityResponseDto>
    suspend fun legacyUpdateSharedActivity(
        sharedActivityId: Long,
        body: SharedActivityActionRequestDto,
    ): ApiResponse<SharedActivityResponseDto>
}
