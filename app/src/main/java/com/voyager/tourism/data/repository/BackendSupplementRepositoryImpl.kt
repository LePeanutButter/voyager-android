package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.BackendMiscApiService
import com.voyager.tourism.data.api.SocialApiService
import com.voyager.tourism.data.api.TravelPlanApiService
import com.voyager.tourism.data.api.UserApiService
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
import com.voyager.tourism.data.dto.UserRegistrationDto
import com.voyager.tourism.data.dto.UserStatisticsDto
import com.voyager.tourism.domain.repository.BackendSupplementRepository
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendSupplementRepositoryImpl @Inject constructor(
    private val userApi: UserApiService,
    private val travelPlanApi: TravelPlanApiService,
    private val socialApi: SocialApiService,
    private val miscApi: BackendMiscApiService,
) : BackendSupplementRepository {

    override suspend fun registerUserAlias(body: UserRegistrationDto): ApiResponse<UserDto> =
        userApi.registerUserAlias(body)

    override suspend fun getUserByUsername(username: String): ApiResponse<UserDto> =
        userApi.getUserByUsername(username)

    override suspend fun getUserByEmail(email: String): ApiResponse<UserDto> =
        userApi.getUserByEmail(email)

    override suspend fun changePassword(id: Long, currentPassword: String, newPassword: String): ApiResponse<Unit> =
        userApi.changePassword(id, currentPassword, newPassword)

    override suspend fun getAllUsers(page: Int, size: Int, sortBy: String, sortDir: String): PagedResponseUserDto =
        userApi.getAllUsers(page, size, sortBy, sortDir)

    override suspend fun getUsersByRole(role: String, page: Int, size: Int): PagedResponseUserDto =
        userApi.getUsersByRole(role, page, size)

    override suspend fun getUsersByStatus(status: String, page: Int, size: Int): PagedResponseUserDto =
        userApi.getUsersByStatus(status, page, size)

    override suspend fun searchUsersByName(searchTerm: String, page: Int, size: Int): PagedResponseUserDto =
        userApi.searchUsersByName(searchTerm, page, size)

    override suspend fun updateUserRole(id: Long, role: String): ApiResponse<UserDto> =
        userApi.updateUserRole(id, role)

    override suspend fun updateUserStatus(id: Long, status: String): ApiResponse<UserDto> =
        userApi.updateUserStatus(id, status)

    override suspend fun getUserStatistics(): ApiResponse<UserStatisticsDto> =
        userApi.getUserStatistics()

    override suspend fun checkUsernameAvailability(username: String): ApiResponse<Boolean> =
        userApi.checkUsernameAvailability(username)

    override suspend fun checkEmailAvailability(email: String): ApiResponse<Boolean> =
        userApi.checkEmailAvailability(email)

    override suspend fun addActivity(planId: Long, body: CreateActivityRequest): ApiResponse<TravelPlanActivityDto> =
        travelPlanApi.addActivity(planId, body)

    override suspend fun updateActivity(
        planId: Long,
        activityId: Long,
        body: UpdateActivityRequest,
    ): ApiResponse<TravelPlanActivityDto> = travelPlanApi.updateActivity(planId, activityId, body)

    override suspend fun deleteActivity(planId: Long, activityId: Long): ApiResponse<Unit> =
        travelPlanApi.deleteActivity(planId, activityId)

    override suspend fun getTravelPlanConnections(planId: Long, status: String): ApiResponse<List<ConnectionDto>> =
        travelPlanApi.getTravelPlanConnections(planId, status)

    override suspend fun addReservation(planId: Long, body: ReservationDto): ApiResponse<ReservationDto> =
        travelPlanApi.addReservation(planId, body)

    override suspend fun getReservations(planId: Long): ApiResponse<List<ReservationDto>> =
        travelPlanApi.getReservations(planId)

    override suspend fun shareTravelPlan(planId: Long): ApiResponse<String> =
        travelPlanApi.shareTravelPlan(planId)

    override suspend fun getSharedTravelPlan(shareToken: String): ApiResponse<TravelPlanDto> =
        travelPlanApi.getSharedTravelPlan(shareToken)

    override suspend fun getTravelPlansByStatus(status: String, page: Int, size: Int): PagedResponseTravelPlanDto =
        travelPlanApi.getTravelPlansByStatus(status, page, size)

    override suspend fun getTravelPlansByType(type: String, page: Int, size: Int): PagedResponseTravelPlanDto =
        travelPlanApi.getTravelPlansByType(type, page, size)

    override suspend fun updateTravelPlanStatus(planId: Long, status: String): ApiResponse<TravelPlanDto> =
        travelPlanApi.updateTravelPlanStatus(planId, status)

    override suspend fun removeConnection(connectionId: Long): ApiResponse<Unit> =
        socialApi.removeConnection(connectionId)

    override suspend fun getTravelerSummary(travelerId: Long): ApiResponse<TravelerSummaryDto> =
        socialApi.getTravelerSummary(travelerId)

    override suspend fun createReview(body: Map<String, Any?>): ApiResponse<Map<String, Any?>> =
        socialApi.createReview(body)

    override suspend fun getReviews(targetType: String, targetId: Long, page: Int, size: Int): Response<ResponseBody> =
        socialApi.getReviews(targetType, targetId, page, size)

    override suspend fun updateReview(reviewId: Long, body: Map<String, Any?>): ApiResponse<Map<String, Any?>> =
        socialApi.updateReview(reviewId, body)

    override suspend fun deleteReview(reviewId: Long): ApiResponse<Unit> =
        socialApi.deleteReview(reviewId)

    override suspend fun getConversations(userId: Long): Response<ResponseBody> =
        socialApi.getConversations(userId)

    override suspend fun getConversationMessages(
        connectionId: Long,
        userId: Long,
        page: Int,
        size: Int,
    ): PagedResponseMessageDto = socialApi.getConversationMessages(connectionId, userId, page, size)

    override suspend fun markMessageAsRead(messageId: Long): ApiResponse<Unit> =
        socialApi.markMessageAsRead(messageId)

    override suspend fun getSocialFeed(userId: Long, page: Int, size: Int): Response<ResponseBody> =
        socialApi.getSocialFeed(userId, page, size)

    override suspend fun createPost(body: Map<String, Any?>): ApiResponse<Map<String, Any?>> =
        socialApi.createPost(body)

    override suspend fun likePost(postId: Long): ApiResponse<Map<String, Any?>> =
        socialApi.likePost(postId)

    override suspend fun unlikePost(postId: Long): ApiResponse<Map<String, Any?>> =
        socialApi.unlikePost(postId)

    override suspend fun commentOnPost(postId: Long, body: Map<String, Any?>): ApiResponse<Map<String, Any?>> =
        socialApi.commentOnPost(postId, body)

    override suspend fun getPostComments(postId: Long, page: Int, size: Int): Response<ResponseBody> =
        socialApi.getPostComments(postId, page, size)

    override suspend fun sendMessage(body: com.voyager.tourism.data.dto.SendMessageRequestDto): ApiResponse<MessageDto> =
        socialApi.sendMessage(body)

    override suspend fun getDestinationMatches(
        destination: String,
        startDate: String,
        endDate: String,
        interests: List<String>?,
        limit: Int,
    ): ApiResponse<List<MatchResponseDto>> =
        miscApi.getDestinationMatches(destination, startDate, endDate, interests, limit)

    override suspend fun legacyShareActivity(
        activityId: Long,
        body: ShareActivityRequestDto,
    ): ApiResponse<SharedActivityResponseDto> = miscApi.legacyShareActivity(activityId, body)

    override suspend fun legacyUpdateSharedActivity(
        sharedActivityId: Long,
        body: SharedActivityActionRequestDto,
    ): ApiResponse<SharedActivityResponseDto> = miscApi.legacyUpdateSharedActivity(sharedActivityId, body)
}
