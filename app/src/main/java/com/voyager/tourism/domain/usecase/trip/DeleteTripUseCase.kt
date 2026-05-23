package com.voyager.tourism.domain.usecase.trip

import com.voyager.tourism.domain.repository.TripRepository
import javax.inject.Inject

/**
 * Use case for deleting a trip
 * Encapsulates the business logic for deleting trip data
 */
class DeleteTripUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    
    /**
     * Delete a trip by ID
     * @param tripId Trip ID to delete
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(tripId: String): Result<Unit> {
        return try {
            if (tripId.isBlank()) {
                Result.failure(IllegalArgumentException("Trip ID cannot be empty"))
            } else {
                tripRepository.deleteTrip(tripId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
