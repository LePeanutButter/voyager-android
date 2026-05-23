package com.voyager.tourism.domain.usecase.trip

import com.voyager.tourism.domain.repository.TripRepository
import com.voyager.tourism.domain.model.Trip
import javax.inject.Inject

/**
 * Use case for updating a trip
 * Encapsulates the business logic for updating trip data
 */
class UpdateTripUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    
    /**
     * Update an existing trip
     * @param trip Updated trip data
     * @return Result containing updated trip or error
     */
    suspend operator fun invoke(trip: Trip): Result<Trip> {
        return try {
            if (trip.id.isBlank()) {
                Result.failure(IllegalArgumentException("Trip ID cannot be empty"))
            } else {
                tripRepository.updateTrip(trip)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
