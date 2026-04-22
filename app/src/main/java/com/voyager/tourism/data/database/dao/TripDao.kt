package com.voyager.tourism.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.voyager.tourism.data.database.entity.TripEntity

/**
 * Data Access Object for Trip operations
 * Provides database access methods for trip entities
 */
@Dao
interface TripDao {
    
    @Query("SELECT * FROM trips WHERE userId = :userId")
    suspend fun getTripsByUserId(userId: String): List<TripEntity>
    
    @Query("SELECT * FROM trips WHERE userId = :userId")
    fun streamTripsByUserId(userId: String): Flow<List<TripEntity>>
    
    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripById(tripId: String): TripEntity?
    
    @Query("SELECT * FROM trips WHERE id = :tripId")
    fun streamTripById(tripId: String): Flow<TripEntity?>
    
    @Query("SELECT * FROM trips WHERE userId = :userId AND status = 'ACTIVE'")
    suspend fun getActiveTrips(userId: String): List<TripEntity>
    
    @Query("SELECT * FROM trips WHERE userId = :userId AND startDate > :currentTime")
    suspend fun getUpcomingTrips(userId: String, currentTime: Long): List<TripEntity>
    
    @Query("SELECT * FROM trips WHERE userId = :userId AND status = 'COMPLETED'")
    suspend fun getCompletedTrips(userId: String): List<TripEntity>
    
    @Query("SELECT * FROM trips WHERE userId = :userId AND destination LIKE '%' || :destination || '%'")
    suspend fun searchTripsByDestination(userId: String, destination: String): List<TripEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrips(trips: List<TripEntity>)
    
    @Update
    suspend fun updateTrip(trip: TripEntity)
    
    @Delete
    suspend fun deleteTrip(trip: TripEntity)
    
    @Query("DELETE FROM trips WHERE id = :tripId")
    suspend fun deleteTripById(tripId: String)
    
    @Query("DELETE FROM trips WHERE userId = :userId")
    suspend fun deleteAllUserTrips(userId: String)
}
