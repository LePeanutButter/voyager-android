package com.voyager.tourism.domain.usecase.trip

import com.voyager.tourism.domain.repository.TripRepository
import com.voyager.tourism.domain.model.Trip
import javax.inject.Inject

/**
 * Use case for getting all trips
 * Encapsulates the business logic for retrieving trip data
 */
class GetTripsUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    
    /**
     * Get all trips for the current user
     * @return Result containing list of trips or error
     */
    suspend operator fun invoke(): Result<List<Trip>> {
        return try {
            // Use getCurrentUserId from auth repository or pass userId parameter
            tripRepository.getUserTrips("") // TODO: Get current user ID
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get trips for a specific user
     * @param userId User ID
     * @return Result containing list of trips or error
     */
    suspend operator fun invoke(userId: String): Result<List<Trip>> {
        return try {
            tripRepository.getUserTrips(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
