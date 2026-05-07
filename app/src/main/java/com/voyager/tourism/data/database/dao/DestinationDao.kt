package com.voyager.tourism.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for destination catalog rows cached on device.
 */
@Dao
interface DestinationDao {
    
    /**
     * Observes all destinations ordered by display name.
     */
    @Query("SELECT * FROM destinations ORDER BY name ASC")
    fun getAllDestinations(): Flow<List<com.voyager.tourism.data.database.entity.DestinationEntity>>
    
    /**
     * Loads a single destination by primary key, or null if missing.
     */
    @Query("SELECT * FROM destinations WHERE id = :id")
    suspend fun getDestinationById(id: String): com.voyager.tourism.data.database.entity.DestinationEntity?
    
    /**
     * Inserts or replaces one destination row.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDestination(destination: com.voyager.tourism.data.database.entity.DestinationEntity)
    
    /**
     * Inserts or replaces multiple destination rows.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDestinations(destinations: List<com.voyager.tourism.data.database.entity.DestinationEntity>)
    
    /**
     * Updates an existing destination row.
     */
    @Update
    suspend fun updateDestination(destination: com.voyager.tourism.data.database.entity.DestinationEntity)
    
    /**
     * Deletes the given destination row.
     */
    @Delete
    suspend fun deleteDestination(destination: com.voyager.tourism.data.database.entity.DestinationEntity)
    
    /**
     * Deletes every row in the destinations table.
     */
    @Query("DELETE FROM destinations")
    suspend fun deleteAllDestinations()
}
