package com.voyager.tourism.domain.repository

import com.voyager.tourism.data.dto.ApiResponse
import com.voyager.tourism.data.dto.ConnectionDto
import com.voyager.tourism.data.dto.CreateActivityRequest
import com.voyager.tourism.data.dto.MatchResponseDto
import com.voyager.tourism.data.dto.MessageDto
import com.voyager.tourism.data.dto.PagedResponseMessageDto
import com.voyager.tourism.data.dto.PagedResponseSocialPostDto
import com.voyager.tourism.data.dto.PagedResponseSocialReviewDto
import com.voyager.tourism.data.dto.PagedResponseTravelPlanDto
import com.voyager.tourism.data.dto.PagedResponseUserDto
import com.voyager.tourism.data.dto.ReservationDto
import com.voyager.tourism.data.dto.ShareActivityRequestDto
import com.voyager.tourism.data.dto.SharedActivityActionRequestDto
import com.voyager.tourism.data.dto.SharedActivityResponseDto
import com.voyager.tourism.data.dto.SocialCommentDto
import com.voyager.tourism.data.dto.SocialConversationDto
import com.voyager.tourism.data.dto.SocialPostDto
import com.voyager.tourism.data.dto.SocialReviewDto
import com.voyager.tourism.data.dto.TravelPlanActivityDto
import com.voyager.tourism.data.dto.TravelPlanDto
import com.voyager.tourism.data.dto.TravelerSummaryDto
import com.voyager.tourism.data.dto.UpdateActivityRequest
import com.voyager.tourism.data.dto.UserDto
import com.voyager.tourism.data.dto.UserStatisticsDto
import com.voyager.tourism.data.dto.UserRegistrationDto
import com.voyager.tourism.data.dto.SendMessageRequestDto

/**
 * Escape hatch for **voyager-backend-core** endpoints not covered by the focused repositories
 * ([UserRepository], [TripRepository], [SocialRepository], [AuthRepository], [TravelRepositoryImpl]).
 * Use cases can call these thin passthroughs without adding business logic in the repository layer.
 */
@Suppress("TooManyFunctions")
interface BackendSupplementRepository {

    // --- UserApiService (supplement) ---
    /** Registers through the alternate route exposed for legacy clients. */
    suspend fun registerUserAlias(body: UserRegistrationDto): ApiResponse<UserDto>

    /** Looks up a profile via username. */
    suspend fun getUserByUsername(username: String): ApiResponse<UserDto>

    /** Looks up a profile via email address. */
    suspend fun getUserByEmail(email: String): ApiResponse<UserDto>

    /** Changes password after validating the old secret. */
    suspend fun changePassword(id: Long, currentPassword: String, newPassword: String): ApiResponse<Unit>

    /** Admin listing with sort metadata. */
    suspend fun getAllUsers(
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "createdAt",
        sortDir: String = "desc",
    ): PagedResponseUserDto

    /** Filters paged users by role keyword. */
    suspend fun getUsersByRole(role: String, page: Int = 0, size: Int = 20): PagedResponseUserDto

    /** Filters paged users by account status. */
    suspend fun getUsersByStatus(status: String, page: Int = 0, size: Int = 20): PagedResponseUserDto

    /** Performs fuzzy name search across user records. */
    suspend fun searchUsersByName(searchTerm: String, page: Int = 0, size: Int = 20): PagedResponseUserDto

    /** Updates authorization levels for a managed account. */
    suspend fun updateUserRole(id: Long, role: String): ApiResponse<UserDto>

    /** Updates lifecycle state (active, suspended, etc.). */
    suspend fun updateUserStatus(id: Long, status: String): ApiResponse<UserDto>

    /** Retrieves global counters for administrator dashboards. */
    suspend fun getUserStatistics(): ApiResponse<UserStatisticsDto>

    /** Checks whether a username is unused. */
    suspend fun checkUsernameAvailability(username: String): ApiResponse<Boolean>

    /** Checks whether an email is unused. */
    suspend fun checkEmailAvailability(email: String): ApiResponse<Boolean>

    // --- TravelPlanApiService (supplement) ---
    /** Adds an itinerary line item to an existing plan. */
    suspend fun addActivity(planId: Long, body: CreateActivityRequest): ApiResponse<TravelPlanActivityDto>

    /** Updates fields on a nested activity entry. */
    suspend fun updateActivity(
        planId: Long,
        activityId: Long,
        body: UpdateActivityRequest,
    ): ApiResponse<TravelPlanActivityDto>

    /** Deletes one activity from the plan timeline. */
    suspend fun deleteActivity(planId: Long, activityId: Long): ApiResponse<Unit>

    /** Lists social connections tied to collaborators on a plan. */
    suspend fun getTravelPlanConnections(planId: Long, status: String = "ACCEPTED"): ApiResponse<List<ConnectionDto>>

    /** Persists reservation metadata for hotels, tours, or tickets. */
    suspend fun addReservation(planId: Long, body: ReservationDto): ApiResponse<ReservationDto>

    /** Returns every stored reservation row for UI summaries. */
    suspend fun getReservations(planId: Long): ApiResponse<List<ReservationDto>>

    /** Generates an opaque token used for public sharing links. */
    suspend fun shareTravelPlan(planId: Long): ApiResponse<String>

    /** Resolves a travel plan that was opened via share token. */
    suspend fun getSharedTravelPlan(shareToken: String): ApiResponse<TravelPlanDto>

    /** Admin search across plans filtered by normalized status. */
    suspend fun getTravelPlansByStatus(status: String, page: Int = 0, size: Int = 20): PagedResponseTravelPlanDto

    /** Admin search filtered by descriptive travel type. */
    suspend fun getTravelPlansByType(type: String, page: Int = 0, size: Int = 20): PagedResponseTravelPlanDto

    /** Performs a focused status mutation without replacing the full DTO. */
    suspend fun updateTravelPlanStatus(planId: Long, status: String): ApiResponse<TravelPlanDto>

    // --- SocialApiService (supplement) ---
    /** Deletes a symmetric connection between two profiles. */
    suspend fun removeConnection(connectionId: Long): ApiResponse<Unit>

    /** Retrieves compact card data for traveler highlight sheets. */
    suspend fun getTravelerSummary(travelerId: Long): ApiResponse<TravelerSummaryDto>

    /** Creates a review with loosely typed payload mirroring backend JSON. */
    suspend fun createReview(body: SocialReviewDto): ApiResponse<SocialReviewDto>

    /** Fetches reviews as raw JSON for custom parsing pipelines. */
    suspend fun getReviews(targetType: String, targetId: Long, page: Int = 0, size: Int = 20): ApiResponse<PagedResponseSocialReviewDto>

    /** Updates review content or rating. */
    suspend fun updateReview(reviewId: Long, body: SocialReviewDto): ApiResponse<SocialReviewDto>

    /** Deletes a published review. */
    suspend fun deleteReview(reviewId: Long): ApiResponse<Unit>

    /** Lists conversations for messaging home screens. */
    suspend fun getConversations(userId: Long): ApiResponse<List<SocialConversationDto>>

    /** Returns paginated DM history for a specific connection pairing. */
    suspend fun getConversationMessages(
        connectionId: Long,
        userId: Long,
        page: Int = 0,
        size: Int = 50,
    ): PagedResponseMessageDto

    /** Marks a chat bubble as consumed for badge counts. */
    suspend fun markMessageAsRead(messageId: Long): ApiResponse<Unit>

    /** Streams timeline cards for the social feed experience. */
    suspend fun getSocialFeed(userId: Long, page: Int = 0, size: Int = 20): ApiResponse<PagedResponseSocialPostDto>

    /** Publishes a new post with arbitrary structured fields. */
    suspend fun createPost(body: SocialPostDto): ApiResponse<SocialPostDto>

    /** Records a like for analytics and counters. */
    suspend fun likePost(postId: Long): ApiResponse<SocialPostDto>

    /** Removes a previously registered like. */
    suspend fun unlikePost(postId: Long): ApiResponse<SocialPostDto>

    /** Appends a threaded comment to a feed post. */
    suspend fun commentOnPost(postId: Long, body: SocialCommentDto): ApiResponse<SocialCommentDto>

    /** Fetches nested comments as raw JSON. */
    suspend fun getPostComments(postId: Long, page: Int = 0, size: Int = 20): ApiResponse<List<SocialCommentDto>>

    /** Sends a peer-to-peer direct message. */
    suspend fun sendMessage(body: SendMessageRequestDto): ApiResponse<MessageDto>

    // --- BackendMiscApiService (supplement) ---
    /** Runs destination/date overlap matching with optional interest hints. */
    suspend fun getDestinationMatches(
        destination: String,
        startDate: String,
        endDate: String,
        interests: List<String>? = null,
        limit: Int = 20,
    ): ApiResponse<List<MatchResponseDto>>

    /** Legacy share route kept for older mobile builds. */
    suspend fun legacyShareActivity(activityId: Long, body: ShareActivityRequestDto): ApiResponse<SharedActivityResponseDto>

    /** Legacy mutation route for shared activity decisions. */
    suspend fun legacyUpdateSharedActivity(
        sharedActivityId: Long,
        body: SharedActivityActionRequestDto,
    ): ApiResponse<SharedActivityResponseDto>
}
