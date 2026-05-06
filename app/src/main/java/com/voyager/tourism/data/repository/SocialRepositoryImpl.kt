package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.BackendMiscApiService
import com.voyager.tourism.data.api.SocialApiService
import com.voyager.tourism.data.api.TravelPlanApiService
import com.voyager.tourism.data.dto.ApiResponse
import com.voyager.tourism.data.dto.CompatibilityMatchRequestDto
import com.voyager.tourism.data.dto.ConnectionRequestDto
import com.voyager.tourism.data.dto.SendConnectionRequestDto
import com.voyager.tourism.data.dto.ShareActivityRequestDto
import com.voyager.tourism.data.dto.SharedActivityActionRequestDto
import com.voyager.tourism.data.dto.TravelerMatchDto
import com.voyager.tourism.domain.model.CompatibilityMatch
import com.voyager.tourism.domain.model.SharedActivity
import com.voyager.tourism.domain.model.SharedActivityDecision
import com.voyager.tourism.domain.model.TravelActivity
import com.voyager.tourism.domain.model.TravelerConnection
import com.voyager.tourism.domain.repository.SocialRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Red social y matching contra **voyager-backend-core**.
 */
@Singleton
class SocialRepositoryImpl @Inject constructor(
    private val socialApi: SocialApiService,
    private val travelPlanApi: TravelPlanApiService,
    private val miscApi: BackendMiscApiService,
) : SocialRepository {

    override suspend fun getCompatibleTravelers(travelPlanId: String, token: String): List<TravelerMatchDto> {
        val id = travelPlanId.toLongOrNull() ?: throw IllegalArgumentException("travelPlanId inválido")
        val resp = travelPlanApi.findCompatibleTravelers(id)
        return requireList(resp)
    }

    override suspend fun sendConnectionRequest(request: SendConnectionRequestDto, token: String): ConnectionRequestDto {
        val resp = socialApi.sendConnectionRequest(request)
        return requireValue(resp)
    }

    override suspend fun acceptConnectionRequest(requestId: String, token: String): ConnectionRequestDto {
        val id = requestId.toLongOrNull() ?: throw IllegalArgumentException("requestId inválido")
        val resp = socialApi.acceptConnectionRequest(id)
        return requireValue(resp)
    }

    override suspend fun rejectConnectionRequest(requestId: String, token: String): ConnectionRequestDto {
        val id = requestId.toLongOrNull() ?: throw IllegalArgumentException("requestId inválido")
        val resp = socialApi.rejectConnectionRequest(id)
        return requireValue(resp)
    }

    override suspend fun getPendingRequests(token: String): List<ConnectionRequestDto> {
        val resp = socialApi.getPendingRequests()
        return requireList(resp)
    }

    override suspend fun getSentRequests(token: String): List<ConnectionRequestDto> {
        val resp = socialApi.getSentRequests()
        return requireList(resp)
    }

    override suspend fun getConnections(userId: Long): Result<List<TravelerConnection>> = runCatching {
        val resp = socialApi.getUserConnections(userId)
        val list = requireList(resp).map {
            TravelerConnection(id = it.userId, username = it.username, status = it.status)
        }
        list
    }

    override suspend fun getTravelPlanActivities(travelPlanId: Long): Result<List<TravelActivity>> = runCatching {
        val resp = travelPlanApi.getActivities(travelPlanId)
        requireList(resp).map { TravelActivity(it.id ?: 0L, it.name.orEmpty()) }
    }

    override suspend fun shareActivity(activityId: Long, receiverId: Long): Result<SharedActivity> = runCatching {
        val resp = miscApi.shareActivity(activityId, ShareActivityRequestDto(receiverId))
        requireValue(resp).toDomain()
    }

    override suspend fun resolveSharedActivity(
        sharedActivityId: Long,
        decision: SharedActivityDecision,
    ): Result<SharedActivity> = runCatching {
        val resp = miscApi.updateSharedActivity(
            sharedActivityId,
            SharedActivityActionRequestDto(decision.name),
        )
        requireValue(resp).toDomain()
    }

    override suspend fun getCompatibilityMatches(
        destination: String,
        startDate: String,
        endDate: String,
        interests: List<String>,
    ): Result<List<CompatibilityMatch>> = runCatching {
        val resp = miscApi.findCompatibilityMatches(
            CompatibilityMatchRequestDto(destination, startDate, endDate, interests),
        )
        requireList(resp).map {
            CompatibilityMatch(
                userId = it.userId,
                totalScore = it.totalScore,
                destinationScore = it.destinationScore,
                dateProximityScore = it.dateProximityScore,
                interestScore = it.interestScore,
                matchedInterests = it.matchedInterests,
            )
        }
    }

    private fun <T> requireValue(response: ApiResponse<T>): T {
        if (response.status in 200..299 && response.data != null) {
            return response.data!!
        }
        throw IllegalStateException(response.message ?: "HTTP ${response.status}")
    }

    private fun <T> requireList(response: ApiResponse<List<T>>): List<T> {
        if (response.status in 200..299) {
            return response.data.orEmpty()
        }
        throw IllegalStateException(response.message ?: "HTTP ${response.status}")
    }

    private fun com.voyager.tourism.data.dto.SharedActivityResponseDto.toDomain() = SharedActivity(
        id = id ?: throw IllegalStateException("Shared activity id is missing"),
        activityId = activityId ?: throw IllegalStateException("Shared activity activityId is missing"),
        senderId = senderId ?: throw IllegalStateException("Shared activity senderId is missing"),
        receiverId = receiverId ?: throw IllegalStateException("Shared activity receiverId is missing"),
        status = status ?: throw IllegalStateException("Shared activity status is missing"),
        sharedPlan = sharedPlan ?: false,
    )
}
