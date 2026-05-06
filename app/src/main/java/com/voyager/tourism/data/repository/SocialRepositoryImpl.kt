package com.voyager.tourism.data.repository

import com.squareup.moshi.Moshi
import com.voyager.tourism.data.api.TourismApiService
import com.voyager.tourism.data.dto.ApiErrorDto
import com.voyager.tourism.data.dto.CompatibilityMatchRequestDto
import com.voyager.tourism.data.dto.ConnectionRequestDto
import com.voyager.tourism.data.dto.SendConnectionRequestDto
import com.voyager.tourism.data.dto.ShareActivityRequestDto
import com.voyager.tourism.data.dto.SharedActivityActionRequestDto
import com.voyager.tourism.data.dto.TravelerMatchDto
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.model.CompatibilityMatch
import com.voyager.tourism.domain.model.SharedActivity
import com.voyager.tourism.domain.model.SharedActivityDecision
import com.voyager.tourism.domain.model.TravelActivity
import com.voyager.tourism.domain.model.TravelerConnection
import com.voyager.tourism.domain.repository.SocialRepository
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SocialRepository interface
 *
 * This repository handles social-related data operations by communicating
 * with the backend API through the TourismApiService.
 */
@Singleton
class SocialRepositoryImpl @Inject constructor(
    private val apiService: TourismApiService,
    private val preferencesManager: PreferencesManager,
    private val moshi: Moshi
) : SocialRepository {

    override suspend fun getCompatibleTravelers(
        travelPlanId: String,
        token: String
    ): List<TravelerMatchDto> {
        val response = apiService.getCompatibleTravelers(travelPlanId, token)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Failed to get compatible travelers: ${response.code()}")
        }
    }

    override suspend fun sendConnectionRequest(
        request: SendConnectionRequestDto,
        token: String
    ): ConnectionRequestDto {
        val response = apiService.sendConnectionRequest(request, token)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to send connection request: ${response.code()}")
        }
    }

    override suspend fun acceptConnectionRequest(
        requestId: String,
        token: String
    ): ConnectionRequestDto {
        val response = apiService.acceptConnectionRequest(requestId, token)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to accept connection request: ${response.code()}")
        }
    }

    override suspend fun rejectConnectionRequest(
        requestId: String,
        token: String
    ): ConnectionRequestDto {
        val response = apiService.rejectConnectionRequest(requestId, token)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to reject connection request: ${response.code()}")
        }
    }

    override suspend fun getPendingRequests(token: String): List<ConnectionRequestDto> {
        val response = apiService.getPendingRequests(token)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Failed to get pending requests: ${response.code()}")
        }
    }

    override suspend fun getSentRequests(token: String): List<ConnectionRequestDto> {
        val response = apiService.getSentRequests(token)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Failed to get sent requests: ${response.code()}")
        }
    }

    override suspend fun getConnections(userId: Long): Result<List<TravelerConnection>> = runCatching {
        val response = apiService.getConnections(userId, authHeader())
        val data = response.requireApiData().map {
            TravelerConnection(id = it.id, username = it.username, status = it.status)
        }
        data
    }

    override suspend fun getTravelPlanActivities(travelPlanId: Long): Result<List<TravelActivity>> = runCatching {
        val response = apiService.getTravelPlanActivities(travelPlanId, authHeader())
        response.requireApiData().map { TravelActivity(it.id, it.name.orEmpty()) }
    }

    override suspend fun shareActivity(activityId: Long, receiverId: Long): Result<SharedActivity> = runCatching {
        val response = apiService.shareActivity(
            activityId = activityId,
            request = ShareActivityRequestDto(receiverId),
            token = authHeader()
        )
        response.requireApiData().toDomain()
    }

    override suspend fun resolveSharedActivity(
        sharedActivityId: Long,
        decision: SharedActivityDecision
    ): Result<SharedActivity> = runCatching {
        val response = apiService.updateSharedActivity(
            sharedActivityId = sharedActivityId,
            request = SharedActivityActionRequestDto(decision.name),
            token = authHeader()
        )
        response.requireApiData().toDomain()
    }

    override suspend fun getCompatibilityMatches(
        destination: String,
        startDate: String,
        endDate: String,
        interests: List<String>
    ): Result<List<CompatibilityMatch>> = runCatching {
        val response = apiService.getCompatibilityMatches(
            request = CompatibilityMatchRequestDto(destination, startDate, endDate, interests),
            token = authHeader()
        )
        response.requireApiData().map {
            CompatibilityMatch(
                userId = it.userId,
                totalScore = it.totalScore,
                destinationScore = it.destinationScore,
                dateProximityScore = it.dateProximityScore,
                interestScore = it.interestScore,
                matchedInterests = it.matchedInterests
            )
        }
    }

    private fun authHeader(): String {
        val token = preferencesManager.getAuthToken()
        require(!token.isNullOrBlank()) { "Authentication token not found. Please login again." }
        return "Bearer $token"
    }

    private fun <T> Response<com.voyager.tourism.data.dto.ApiResponseDto<T>>.requireApiData(): T {
        if (isSuccessful) {
            val body = body()
            return body?.data ?: throw IllegalStateException(body?.message ?: "Empty response")
        }
        val message = errorBody()?.string()?.let { raw ->
            parseErrorMessage(raw)
        } ?: "Request failed with status ${code()}"
        throw IllegalStateException(message)
    }

    private fun parseErrorMessage(raw: String): String? {
        return try {
            moshi.adapter(ApiErrorDto::class.java).fromJson(raw)?.message
                ?: moshi.adapter(com.voyager.tourism.data.dto.ApiResponseDto::class.java).fromJson(raw)?.message
        } catch (_: IOException) {
            null
        } catch (_: RuntimeException) {
            null
        }
    }

    private fun com.voyager.tourism.data.dto.SharedActivityResponseDto.toDomain() = SharedActivity(
        id = id ?: throw IllegalStateException("Shared activity id is missing"),
        activityId = activityId ?: throw IllegalStateException("Shared activity activityId is missing"),
        senderId = senderId ?: throw IllegalStateException("Shared activity senderId is missing"),
        receiverId = receiverId ?: throw IllegalStateException("Shared activity receiverId is missing"),
        status = status ?: throw IllegalStateException("Shared activity status is missing"),
        sharedPlan = sharedPlan ?: false
    )
}
