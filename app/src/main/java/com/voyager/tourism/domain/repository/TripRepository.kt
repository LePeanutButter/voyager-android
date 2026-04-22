package com.voyager.tourism.domain.repository

import com.voyager.tourism.domain.model.Trip
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for trip-related operations
 */
interface TripRepository {
    
    /**
     * Get all trips for a user
     */
    suspend fun getUserTrips(userId: String): Result<List<Trip>>
    
    /**
     * Get trip by ID
     */
    suspend fun getTripById(tripId: String): Result<Trip?>
    
    /**
     * Create new trip
     */
    suspend fun createTrip(trip: Trip): Result<Trip>
    
    /**
     * Update existing trip
     */
    suspend fun updateTrip(trip: Trip): Result<Trip>
    
    /**
     * Delete trip
     */
    suspend fun deleteTrip(tripId: String): Result<Unit>
    
    /**
     * Get active trips
     */
    suspend fun getActiveTrips(userId: String): Result<List<Trip>>
    
    /**
     * Get upcoming trips
     */
    suspend fun getUpcomingTrips(userId: String): Result<List<Trip>>
    
    /**
     * Get completed trips
     */
    suspend fun getCompletedTrips(userId: String): Result<List<Trip>>
    
    /**
     * Search trips by destination
     */
    suspend fun searchTripsByDestination(userId: String, destination: String): Result<List<Trip>>
    
    /**
     * Stream trip updates
     */
    fun streamTripUpdates(tripId: String): Flow<Trip?>
}
