package com.voyager.tourism.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for destinations
 * Handles database operations for destination entities
 */
@Dao
interface DestinationDao {
    
    @Query("SELECT * FROM destinations ORDER BY name ASC")
    fun getAllDestinations(): Flow<List<com.voyager.tourism.data.database.entity.DestinationEntity>>
    
    @Query("SELECT * FROM destinations WHERE id = :id")
    suspend fun getDestinationById(id: String): com.voyager.tourism.data.database.entity.DestinationEntity?
    
        
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDestination(destination: com.voyager.tourism.data.database.entity.DestinationEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDestinations(destinations: List<com.voyager.tourism.data.database.entity.DestinationEntity>)
    
    @Update
    suspend fun updateDestination(destination: com.voyager.tourism.data.database.entity.DestinationEntity)
    
    @Delete
    suspend fun deleteDestination(destination: com.voyager.tourism.data.database.entity.DestinationEntity)
    
    @Query("DELETE FROM destinations")
    suspend fun deleteAllDestinations()
}
