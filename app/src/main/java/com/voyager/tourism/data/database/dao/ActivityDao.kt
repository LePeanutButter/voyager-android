package com.voyager.tourism.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.voyager.tourism.data.database.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

/** Room DAO for cached travel-plan activities tied to a trip or travel plan. */
@Dao
interface ActivityDao {

    /**
     * Observes all activities for the given travel plan identifier, ordered by start time ascending.
     *
     * @param tripId Travel plan identifier (`travelPlanId` column).
     */
    @Query("SELECT * FROM activities WHERE travelPlanId = :tripId ORDER BY startTime ASC")
    fun getActivitiesFlow(tripId: String): Flow<List<ActivityEntity>>

    /**
     * Inserts or replaces a batch of activity rows.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(activities: List<ActivityEntity>)

    /**
     * Inserts or replaces a single activity row.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(activity: ActivityEntity)
}
