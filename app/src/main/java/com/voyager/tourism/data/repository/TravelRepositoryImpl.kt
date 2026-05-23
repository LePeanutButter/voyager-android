package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.TravelApiService
import com.voyager.tourism.data.dto.PagedResponseTravelPlanDto
import com.voyager.tourism.data.dto.TravelPlanDto
import com.voyager.tourism.data.dto.TravelPlanRequest
import com.voyager.tourism.data.dto.TravelPlanStatus
import com.voyager.tourism.data.dto.TravelType
import com.voyager.tourism.domain.repository.TravelRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Travel plan CRUD backed by [TravelApiService] and DTO [TravelPlanDto] types.
 */
@Singleton
class TravelRepositoryImpl @Inject constructor(
    private val travelApiService: TravelApiService,
) : TravelRepository {

    private companion object {
        const val INVALID_PLAN_ID_MSG = "planId inválido"
        const val INVALID_USER_ID_MSG = "userId inválido"
    }

    /** @see TravelRepository.createTravelPlan */
    override suspend fun createTravelPlan(request: TravelPlanRequest): Result<TravelPlanDto> {
        return try {
            val response = travelApiService.createTravelPlan(request.toTravelPlanDto())
            val data = response.data
            if ((response.status == 200 || response.status == 201) && data != null) {
                Result.success(data)
            } else {
                Result.failure(Exception(response.message.ifBlank { "Failed to create travel plan" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** @see TravelRepository.getUserTravelPlans */
    override suspend fun getUserTravelPlans(userId: String): Result<List<TravelPlanDto>> {
        return try {
            val uid = userId.toLongOrNull()
                ?: return Result.failure(IllegalArgumentException(INVALID_USER_ID_MSG))
            val response: PagedResponseTravelPlanDto = travelApiService.getUserTravelPlans(uid)
            if (response.status == 200) {
                Result.success(response.data.orEmpty())
            } else {
                Result.failure(Exception(response.message.ifBlank { "Failed to get travel plans" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** @see TravelRepository.getTravelPlanById */
    override suspend fun getTravelPlanById(planId: String): Result<TravelPlanDto> {
        return try {
            val id = planId.toLongOrNull()
                ?: return Result.failure(IllegalArgumentException(INVALID_PLAN_ID_MSG))
            val response = travelApiService.getTravelPlanById(id)
            val data = response.data
            if (response.status == 200 && data != null) {
                Result.success(data)
            } else {
                Result.failure(Exception(response.message.ifBlank { "Failed to get travel plan" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** @see TravelRepository.updateTravelPlan */
    override suspend fun updateTravelPlan(planId: String, request: TravelPlanRequest): Result<TravelPlanDto> {
        return try {
            val id = planId.toLongOrNull()
                ?: return Result.failure(IllegalArgumentException(INVALID_PLAN_ID_MSG))
            val response = travelApiService.updateTravelPlan(id, request.toTravelPlanDto().copy(id = id))
            val data = response.data
            if (response.status == 200 && data != null) {
                Result.success(data)
            } else {
                Result.failure(Exception(response.message.ifBlank { "Failed to update travel plan" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** @see TravelRepository.deleteTravelPlan */
    override suspend fun deleteTravelPlan(planId: String): Result<Unit> {
        return try {
            val id = planId.toLongOrNull()
                ?: return Result.failure(IllegalArgumentException(INVALID_PLAN_ID_MSG))
            val response = travelApiService.deleteTravelPlan(id)
            if (response.status == 200) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message.ifBlank { "Failed to delete travel plan" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Maps creation requests into a default-draft [TravelPlanDto] used by the REST facade.
     */
    private fun TravelPlanRequest.toTravelPlanDto(): TravelPlanDto = TravelPlanDto(
        title = title,
        description = description,
        status = TravelPlanStatus.ACTIVE,
        travelType = TravelType.LEISURE,
        startDate = startDate,
        endDate = endDate,
        estimatedBudget = estimatedBudget,
        numberOfTravelers = numberOfTravelers,
        originLocation = originLocation,
        destinationLocation = destinationLocation,
    )
}
