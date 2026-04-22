package com.voyager.tourism.domain.usecase.trip

import com.voyager.tourism.domain.model.Trip
import com.voyager.tourism.domain.model.TripStatus
import com.voyager.tourism.domain.repository.TripRepository
import javax.inject.Inject

/**
 * Use case for creating a new trip
 * Encapsulates business logic for trip creation and validation
 */
class CreateTripUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    
    /**
     * Create a new trip with validation
     * @param trip Trip object to create
     * @return Result containing created Trip or error
     */
    suspend operator fun invoke(trip: Trip): Result<Trip> {
        // Validate trip data
        val validationResult = validateTrip(trip)
        if (validationResult != null) {
            return Result.failure(validationResult)
        }
        
        return try {
            // Set initial status to PLANNING if not set
            val tripToCreate = trip.copy(
                status = TripStatus.PLANNING,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            tripRepository.createTrip(tripToCreate)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validate trip data before creation
     */
    private fun validateTrip(trip: Trip): Exception? {
        if (trip.title.isBlank()) {
            return IllegalArgumentException("Trip title cannot be empty")
        }
        if (trip.userId.isBlank()) {
            return IllegalArgumentException("User ID cannot be empty")
        }
        if (trip.destination.name.isBlank()) {
            return IllegalArgumentException("Destination name cannot be empty")
        }
        if (trip.startDate >= trip.endDate) {
            return IllegalArgumentException("Start date must be before end date")
        }
        if (trip.budget <= 0) {
            return IllegalArgumentException("Budget must be positive")
        }
        if (trip.travelers <= 0) {
            return IllegalArgumentException("Number of travelers must be positive")
        }
        
        return null
    }
}
