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
import com.voyager.tourism.data.dto.PagedResponseSocialReviewDto
import com.voyager.tourism.data.dto.PagedResponseSocialPostDto
import com.voyager.tourism.data.dto.UserStatisticsDto
import com.voyager.tourism.data.dto.SocialCommentDto
import com.voyager.tourism.data.dto.SocialConversationDto
import com.voyager.tourism.data.dto.SocialPostDto
import com.voyager.tourism.data.dto.SocialReviewDto
import com.voyager.tourism.domain.repository.BackendSupplementRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin Hilt singleton that forwards [BackendSupplementRepository] calls to Retrofit API facades.
 */
@Singleton
class BackendSupplementRepositoryImpl @Inject constructor(
    private val userApi: UserApiService,
    private val travelPlanApi: TravelPlanApiService,
    private val socialApi: SocialApiService,
    private val miscApi: BackendMiscApiService,
) : BackendSupplementRepository {
    /** @see BackendSupplementRepository.registerUserAlias */
    override suspend fun registerUserAlias(body: UserRegistrationDto): ApiResponse<UserDto> =
        userApi.registerUserAlias(body)

    /** @see BackendSupplementRepository.getUserByUsername */
    override suspend fun getUserByUsername(username: String): ApiResponse<UserDto> =
        userApi.getUserByUsername(username)

    /** @see BackendSupplementRepository.getUserByEmail */
    override suspend fun getUserByEmail(email: String): ApiResponse<UserDto> =
        userApi.getUserByEmail(email)

    /** @see BackendSupplementRepository.changePassword */
    override suspend fun changePassword(id: Long, currentPassword: String, newPassword: String): ApiResponse<Unit> =
        userApi.changePassword(id, currentPassword, newPassword)

    /** @see BackendSupplementRepository.getAllUsers */
    override suspend fun getAllUsers(page: Int, size: Int, sortBy: String, sortDir: String): PagedResponseUserDto =
        userApi.getAllUsers(page, size, sortBy, sortDir)

    /** @see BackendSupplementRepository.getUsersByRole */
    override suspend fun getUsersByRole(role: String, page: Int, size: Int): PagedResponseUserDto =
        userApi.getUsersByRole(role, page, size)

    /** @see BackendSupplementRepository.getUsersByStatus */
    override suspend fun getUsersByStatus(status: String, page: Int, size: Int): PagedResponseUserDto =
        userApi.getUsersByStatus(status, page, size)

    /** @see BackendSupplementRepository.searchUsersByName */
    override suspend fun searchUsersByName(searchTerm: String, page: Int, size: Int): PagedResponseUserDto =
        userApi.searchUsersByName(searchTerm, page, size)

    /** @see BackendSupplementRepository.updateUserRole */
    override suspend fun updateUserRole(id: Long, role: String): ApiResponse<UserDto> =
        userApi.updateUserRole(id, role)

    /** @see BackendSupplementRepository.updateUserStatus */
    override suspend fun updateUserStatus(id: Long, status: String): ApiResponse<UserDto> =
        userApi.updateUserStatus(id, status)

    /** @see BackendSupplementRepository.getUserStatistics */
    override suspend fun getUserStatistics(): ApiResponse<UserStatisticsDto> =
        userApi.getUserStatistics()

    /** @see BackendSupplementRepository.checkUsernameAvailability */
    override suspend fun checkUsernameAvailability(username: String): ApiResponse<Boolean> =
        userApi.checkUsernameAvailability(username)

    /** @see BackendSupplementRepository.checkEmailAvailability */
    override suspend fun checkEmailAvailability(email: String): ApiResponse<Boolean> =
        userApi.checkEmailAvailability(email)

    /** @see BackendSupplementRepository.addActivity */
    override suspend fun addActivity(planId: Long, body: CreateActivityRequest): ApiResponse<TravelPlanActivityDto> =
        travelPlanApi.addActivity(planId, body)

    /** @see BackendSupplementRepository.updateActivity */
    override suspend fun updateActivity(
        planId: Long,
        activityId: Long,
        body: UpdateActivityRequest,
    ): ApiResponse<TravelPlanActivityDto> = travelPlanApi.updateActivity(planId, activityId, body)

    /** @see BackendSupplementRepository.deleteActivity */
    override suspend fun deleteActivity(planId: Long, activityId: Long): ApiResponse<Unit> =
        travelPlanApi.deleteActivity(planId, activityId)

    /** @see BackendSupplementRepository.getTravelPlanConnections */
    override suspend fun getTravelPlanConnections(planId: Long, status: String): ApiResponse<List<ConnectionDto>> =
        travelPlanApi.getTravelPlanConnections(planId, status)

    /** @see BackendSupplementRepository.addReservation */
    override suspend fun addReservation(planId: Long, body: ReservationDto): ApiResponse<ReservationDto> =
        travelPlanApi.addReservation(planId, body)

    /** @see BackendSupplementRepository.getReservations */
    override suspend fun getReservations(planId: Long): ApiResponse<List<ReservationDto>> =
        travelPlanApi.getReservations(planId)

    /** @see BackendSupplementRepository.shareTravelPlan */
    override suspend fun shareTravelPlan(planId: Long): ApiResponse<String> =
        travelPlanApi.shareTravelPlan(planId)

    /** @see BackendSupplementRepository.getSharedTravelPlan */
    override suspend fun getSharedTravelPlan(shareToken: String): ApiResponse<TravelPlanDto> =
        travelPlanApi.getSharedTravelPlan(shareToken)

    /** @see BackendSupplementRepository.getTravelPlansByStatus */
    override suspend fun getTravelPlansByStatus(status: String, page: Int, size: Int): PagedResponseTravelPlanDto =
        travelPlanApi.getTravelPlansByStatus(status, page, size)

    /** @see BackendSupplementRepository.getTravelPlansByType */
    override suspend fun getTravelPlansByType(type: String, page: Int, size: Int): PagedResponseTravelPlanDto =
        travelPlanApi.getTravelPlansByType(type, page, size)

    /** @see BackendSupplementRepository.updateTravelPlanStatus */
    override suspend fun updateTravelPlanStatus(planId: Long, status: String): ApiResponse<TravelPlanDto> =
        travelPlanApi.updateTravelPlanStatus(planId, status)

    /** @see BackendSupplementRepository.removeConnection */
    override suspend fun removeConnection(connectionId: Long): ApiResponse<Unit> =
        socialApi.removeConnection(connectionId)

    /** @see BackendSupplementRepository.getTravelerSummary */
    override suspend fun getTravelerSummary(travelerId: Long): ApiResponse<TravelerSummaryDto> =
        socialApi.getTravelerSummary(travelerId)

    /** @see BackendSupplementRepository.createReview */
    override suspend fun createReview(body: SocialReviewDto): ApiResponse<SocialReviewDto> =
        socialApi.createReview(body)

    /** @see BackendSupplementRepository.getReviews */
    override suspend fun getReviews(targetType: String, targetId: Long, page: Int, size: Int): ApiResponse<PagedResponseSocialReviewDto> =
        socialApi.getReviews(targetType, targetId, page, size)

    /** @see BackendSupplementRepository.updateReview */
    override suspend fun updateReview(reviewId: Long, body: SocialReviewDto): ApiResponse<SocialReviewDto> =
        socialApi.updateReview(reviewId, body)

    /** @see BackendSupplementRepository.deleteReview */
    override suspend fun deleteReview(reviewId: Long): ApiResponse<Unit> =
        socialApi.deleteReview(reviewId)

    /** @see BackendSupplementRepository.getConversations */
    override suspend fun getConversations(userId: Long): ApiResponse<List<SocialConversationDto>> =
        socialApi.getConversations(userId)

    /** @see BackendSupplementRepository.getConversationMessages */
    override suspend fun getConversationMessages(
        connectionId: Long,
        userId: Long,
        page: Int,
        size: Int,
    ): PagedResponseMessageDto = socialApi.getConversationMessages(connectionId, userId, page, size)

    /** @see BackendSupplementRepository.markMessageAsRead */
    override suspend fun markMessageAsRead(messageId: Long): ApiResponse<Unit> =
        socialApi.markMessageAsRead(messageId)

    /** @see BackendSupplementRepository.getSocialFeed */
    override suspend fun getSocialFeed(userId: Long, page: Int, size: Int): ApiResponse<PagedResponseSocialPostDto> =
        socialApi.getSocialFeed(userId, page, size)

    /** @see BackendSupplementRepository.createPost */
    override suspend fun createPost(body: SocialPostDto): ApiResponse<SocialPostDto> =
        socialApi.createPost(body)

    /** @see BackendSupplementRepository.likePost */
    override suspend fun likePost(postId: Long): ApiResponse<SocialPostDto> =
        socialApi.likePost(postId)

    /** @see BackendSupplementRepository.unlikePost */
    override suspend fun unlikePost(postId: Long): ApiResponse<SocialPostDto> =
        socialApi.unlikePost(postId)

    /** @see BackendSupplementRepository.commentOnPost */
    override suspend fun commentOnPost(postId: Long, body: SocialCommentDto): ApiResponse<SocialCommentDto> =
        socialApi.commentOnPost(postId, body)

    /** @see BackendSupplementRepository.getPostComments */
    override suspend fun getPostComments(postId: Long, page: Int, size: Int): ApiResponse<List<SocialCommentDto>> =
        socialApi.getPostComments(postId, page, size)

    /** @see BackendSupplementRepository.sendMessage */
    override suspend fun sendMessage(body: com.voyager.tourism.data.dto.SendMessageRequestDto): ApiResponse<MessageDto> =
        socialApi.sendMessage(body)

    /** @see BackendSupplementRepository.getDestinationMatches */
    override suspend fun getDestinationMatches(
        destination: String,
        startDate: String,
        endDate: String,
        interests: List<String>?,
        limit: Int,
    ): ApiResponse<List<MatchResponseDto>> =
        miscApi.getDestinationMatches(destination, startDate, endDate, interests, limit)

    /** @see BackendSupplementRepository.legacyShareActivity */
    override suspend fun legacyShareActivity(
        activityId: Long,
        body: ShareActivityRequestDto,
    ): ApiResponse<SharedActivityResponseDto> = miscApi.legacyShareActivity(activityId, body)

    /** @see BackendSupplementRepository.legacyUpdateSharedActivity */
    override suspend fun legacyUpdateSharedActivity(
        sharedActivityId: Long,
        body: SharedActivityActionRequestDto,
    ): ApiResponse<SharedActivityResponseDto> = miscApi.legacyUpdateSharedActivity(sharedActivityId, body)
}
