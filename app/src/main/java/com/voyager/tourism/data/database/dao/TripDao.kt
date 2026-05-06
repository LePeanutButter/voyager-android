package com.voyager.tourism.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.voyager.tourism.data.database.entity.TripEntity

/**
 * Room DAO for trip rows synchronized or cached locally.
 */
@Dao
interface TripDao {
    
    /**
     * Loads all trips belonging to the given user identifier.
     */
    @Query("SELECT * FROM trips WHERE userId = :userId")
    suspend fun getTripsByUserId(userId: String): List<TripEntity>
    
    /**
     * Observes all trips for a user as a cold [Flow].
     */
    @Query("SELECT * FROM trips WHERE userId = :userId")
    fun streamTripsByUserId(userId: String): Flow<List<TripEntity>>
    
    /**
     * Loads a single trip by id, or null if not found.
     */
    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripById(tripId: String): TripEntity?
    
    /**
     * Observes a single trip by id, emitting null until found or if deleted.
     */
    @Query("SELECT * FROM trips WHERE id = :tripId")
    fun streamTripById(tripId: String): Flow<TripEntity?>
    
    /**
     * Returns trips with active status for the user.
     */
    @Query("SELECT * FROM trips WHERE userId = :userId AND status = 'ACTIVE'")
    suspend fun getActiveTrips(userId: String): List<TripEntity>
    
    /**
     * Returns trips whose start date is strictly after [currentTime].
     */
    @Query("SELECT * FROM trips WHERE userId = :userId AND startDate > :currentTime")
    suspend fun getUpcomingTrips(userId: String, currentTime: Long): List<TripEntity>
    
    /**
     * Returns completed trips for the user.
     */
    @Query("SELECT * FROM trips WHERE userId = :userId AND status = 'COMPLETED'")
    suspend fun getCompletedTrips(userId: String): List<TripEntity>
    
    /**
     * Performs a case-sensitive substring match on the destination field.
     */
    @Query("SELECT * FROM trips WHERE userId = :userId AND destination LIKE '%' || :destination || '%'")
    suspend fun searchTripsByDestination(userId: String, destination: String): List<TripEntity>
    
    /**
     * Inserts or replaces a single trip row.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity)
    
    /**
     * Inserts or replaces multiple trip rows.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrips(trips: List<TripEntity>)
    
    /**
     * Updates an existing trip row.
     */
    @Update
    suspend fun updateTrip(trip: TripEntity)
    
    /**
     * Deletes the given trip row.
     */
    @Delete
    suspend fun deleteTrip(trip: TripEntity)
    
    /**
     * Deletes a trip by primary key.
     */
    @Query("DELETE FROM trips WHERE id = :tripId")
    suspend fun deleteTripById(tripId: String)
    
    /**
     * Deletes all trips that belong to the given user.
     */
    @Query("DELETE FROM trips WHERE userId = :userId")
    suspend fun deleteAllUserTrips(userId: String)
    
    /**
     * Deletes every row from the trips table.
     */
    @Query("DELETE FROM trips")
    suspend fun deleteAllTrips()
}
