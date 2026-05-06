package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.TravelApiService
import com.voyager.tourism.data.dto.TravelPlanDto
import com.voyager.tourism.data.dto.TravelPlanRequest
import com.voyager.tourism.domain.repository.TravelRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TravelRepository interface
 * Handles travel plan data operations using the API service
 */
@Singleton
class TravelRepositoryImpl @Inject constructor(
    private val travelApiService: TravelApiService
) : TravelRepository {
    
    override suspend fun createTravelPlan(request: TravelPlanRequest): Result<TravelPlanDto> {
        return try {
            val response = travelApiService.createTravelPlan(request)
            
            if (response.status == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to create travel plan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUserTravelPlans(userId: String): Result<List<TravelPlanDto>> {
        return try {
            val response = travelApiService.getUserTravelPlans(userId)
            
            if (response.status == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to get travel plans"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getTravelPlanById(planId: String): Result<TravelPlanDto> {
        return try {
            val response = travelApiService.getTravelPlanById(planId)
            
            if (response.status == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to get travel plan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateTravelPlan(planId: String, request: TravelPlanRequest): Result<TravelPlanDto> {
        return try {
            val response = travelApiService.updateTravelPlan(planId, request)
            
            if (response.status == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to update travel plan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteTravelPlan(planId: String): Result<Unit> {
        return try {
            val response = travelApiService.deleteTravelPlan(planId)
            
            if (response.status == 200) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Failed to delete travel plan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
