package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.TourismApiService
import com.voyager.tourism.data.database.dao.TripDao
import com.voyager.tourism.data.mapper.TripMapper
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.model.Trip
import com.voyager.tourism.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TripRepository interface
 * Handles trip data operations combining remote API and local database
 */
@Singleton
class TripRepositoryImpl @Inject constructor(
    private val apiService: TourismApiService,
    private val tripDao: TripDao,
    private val tripMapper: TripMapper,
    private val preferencesManager: PreferencesManager
) : TripRepository {
    
    override suspend fun getTrips(): Result<List<Trip>> {
        return try {
            val token = preferencesManager.getAuthToken()
            if (token.isNullOrEmpty()) {
                // Try to get from local cache
                val localTrips = tripDao.getAllTrips()
                Result.success(localTrips.map { tripMapper.toDomain(it) })
            } else {
                val response = apiService.getTrips("Bearer $token")
                if (response.isSuccessful) {
                    val trips = response.body()
                    trips?.let {
                        // Cache trips locally
                        tripDao.deleteAllTrips()
                        tripDao.insertTrips(it.map { tripMapper.toEntity(it) })
                        Result.success(it)
                    } ?: Result.failure(Exception("Empty response"))
                } else {
                    // Try to get from local cache if API fails
                    val localTrips = tripDao.getAllTrips()
                    localTrips.let { Result.success(it.map { tripMapper.toDomain(it) }) }
                        ?: Result.failure(Exception("No trip data available"))
                }
            }
        } catch (e: Exception) {
            // Try local cache as fallback
            try {
                val localTrips = tripDao.getAllTrips()
                Result.success(localTrips.map { tripMapper.toDomain(it) })
            } catch (ex: Exception) {
                Result.failure(ex)
            }
        }
    }
    
    override suspend fun getTripById(tripId: String): Result<Trip?> {
        return try {
            val token = preferencesManager.getAuthToken()
            if (token.isNullOrEmpty()) {
                // Try to get from local cache
                val localTrip = tripDao.getTripById(tripId)
                localTrip?.let { tripMapper.toDomain(it) }?.let { Result.success(it) } ?: Result.success(null)
            } else {
                val response = apiService.getTripById(tripId, "Bearer $token")
                if (response.isSuccessful) {
                    val tripDto = response.body()
                    tripDto?.let {
                        // Cache trip locally
                        tripDao.insertTrip(tripMapper.toEntity(it))
                        Result.success(tripMapper.toDomain(it))
                    } ?: Result.success(null)
                } else {
                    // Try to get from local cache if API fails
                    val localTrip = tripDao.getTripById(tripId)
                    Result.success(localTrip?.let { tripMapper.toDomain(it) })
                }
            }
        } catch (e: Exception) {
            // Try local cache as fallback
            val localTrip = tripDao.getTripById(tripId)
            Result.success(localTrip?.let { tripMapper.toDomain(it) })
        }
    }
    
    override suspend fun createTrip(trip: Trip): Result<Trip> {
        return try {
            val token = preferencesManager.getAuthToken()
            if (token.isNullOrEmpty()) {
                Result.failure(Exception("User not authenticated"))
            } else {
                val response = apiService.createTrip(tripMapper.toDto(trip), "Bearer $token")
                if (response.isSuccessful) {
                    val tripDto = response.body()
                    tripDto?.let {
                        // Cache trip locally
                        tripDao.insertTrip(tripMapper.toEntity(it))
                        Result.success(tripMapper.toDomain(it))
                    } ?: Result.failure(Exception("Failed to create trip"))
                } else {
                    Result.failure(Exception("Failed to create trip"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateTrip(trip: Trip): Result<Trip> {
        return try {
            val token = preferencesManager.getAuthToken()
            if (token.isNullOrEmpty()) {
                Result.failure(Exception("User not authenticated"))
            } else {
                val response = apiService.updateTrip(trip.id, tripMapper.toDto(trip), "Bearer $token")
                if (response.isSuccessful) {
                    val tripDto = response.body()
                    tripDto?.let {
                        // Update trip locally
                        tripDao.updateTrip(tripMapper.toEntity(it))
                        Result.success(tripMapper.toDomain(it))
                    } ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Failed to update trip"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteTrip(tripId: String): Result<Unit> {
        return try {
            val token = preferencesManager.getAuthToken()
            if (token.isNullOrEmpty()) {
                Result.failure(Exception("User not authenticated"))
            } else {
                val response = apiService.deleteTrip(tripId, "Bearer $token")
                if (response.isSuccessful) {
                    // Delete trip locally
                    tripDao.deleteTripById(tripId)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to delete trip"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUserTrips(userId: String): Result<List<Trip>> {
        return try {
            val token = preferencesManager.getAuthToken()
            if (token.isNullOrEmpty()) {
                // Try to get from local cache
                val localTrips = tripDao.getTripsByUserId(userId)
                Result.success(localTrips.map { tripMapper.toDomain(it) })
            } else {
                val response = apiService.getUserTrips(userId, "Bearer $token")
                if (response.isSuccessful) {
                    val tripDtos = response.body()
                    tripDtos?.let {
                        // Cache trips locally
                        tripDao.deleteAllTrips()
                        tripDao.insertTrips(it.map { tripMapper.toEntity(it) })
                        Result.success(it.map { tripMapper.toDomain(it) })
                    } ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Failed to get user trips"))
                }
            }
        } catch (e: Exception) {
            // Try local cache as fallback
            val localTrips = tripDao.getTripsByUserId(userId)
            localTrips.let { Result.success(it.map { tripMapper.toDomain(it) }) }
                ?: Result.failure(e)
        }
    }
    
    override suspend fun getActiveTrips(userId: String): Result<List<Trip>> {
        return try {
            val token = preferencesManager.getAuthToken()
            if (token.isNullOrEmpty()) {
                Result.failure(Exception("User not authenticated"))
            } else {
                val response = apiService.getActiveTrips("Bearer $token")
                if (response.isSuccessful) {
                    val tripDtos = response.body()
                    tripDtos?.let { tripDtos ->
                        try {
                            Result.success(tripDtos.map { tripMapper.toDomain(it) })
                        } catch (e: Exception) {
                            Result.failure(e)
                        }
                    } ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Failed to get active trips"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
