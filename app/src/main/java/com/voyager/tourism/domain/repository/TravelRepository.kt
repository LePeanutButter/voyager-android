package com.voyager.tourism.domain.repository

import com.voyager.tourism.data.dto.TravelPlanDto
import com.voyager.tourism.data.dto.TravelPlanRequest

/**
 * Repository interface for travel plan operations
 * Defines the contract for travel plan data layer
 */
interface TravelRepository {
    
    /**
     * Create a new travel plan
     * @param request Travel plan creation request
     * @return Result containing created TravelPlanDto or error
     */
    suspend fun createTravelPlan(request: TravelPlanRequest): Result<TravelPlanDto>
    
    /**
     * Get travel plans for a user
     * @param userId User ID
     * @return Result containing list of TravelPlanDto or error
     */
    suspend fun getUserTravelPlans(userId: String): Result<List<TravelPlanDto>>
    
    /**
     * Get travel plan by ID
     * @param planId Travel plan ID
     * @return Result containing TravelPlanDto or error
     */
    suspend fun getTravelPlanById(planId: String): Result<TravelPlanDto>
    
    /**
     * Update travel plan
     * @param planId Travel plan ID
     * @param request Updated travel plan request
     * @return Result containing updated TravelPlanDto or error
     */
    suspend fun updateTravelPlan(planId: String, request: TravelPlanRequest): Result<TravelPlanDto>
    
    /**
     * Delete travel plan
     * @param planId Travel plan ID
     * @return Result indicating success or failure
     */
    suspend fun deleteTravelPlan(planId: String): Result<Unit>
}
