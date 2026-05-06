package com.voyager.tourism.domain.usecase.trip

import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.TripRepository
import com.voyager.tourism.domain.model.Trip
import javax.inject.Inject

/**
 * Use case for getting all trips
 * Encapsulates the business logic for retrieving trip data
 */
class GetTripsUseCase @Inject constructor(
    private val tripRepository: TripRepository,
    private val preferencesManager: PreferencesManager,
) {
    
    /**
     * Get all trips for the current user (uses stored user id).
     */
    suspend operator fun invoke(): Result<List<Trip>> {
        val userId = preferencesManager.getCurrentUserId()
            ?: return Result.failure(IllegalStateException("No hay sesión: falta user id"))
        return invoke(userId)
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
