package com.voyager.tourism.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.voyager.tourism.data.database.Converters
import com.voyager.tourism.data.database.dao.UserDao
import com.voyager.tourism.data.database.dao.TripDao
import com.voyager.tourism.data.database.dao.DestinationDao
import com.voyager.tourism.data.database.dao.ActivityDao
import com.voyager.tourism.data.database.entity.UserEntity
import com.voyager.tourism.data.database.entity.TripEntity
import com.voyager.tourism.data.database.entity.DestinationEntity
import com.voyager.tourism.data.database.entity.ItineraryItemEntity
import com.voyager.tourism.data.database.entity.ActivityEntity

/**
 * Room database for Tourism Intelligent Platform
 * Provides local storage for user data, trips, destinations, and itinerary items
 */
@Database(
    entities = [
        UserEntity::class,
        TripEntity::class,
        DestinationEntity::class,
        ItineraryItemEntity::class,
        ActivityEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TourismDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun tripDao(): TripDao
    abstract fun destinationDao(): DestinationDao
    abstract fun activityDao(): ActivityDao
    
    companion object {
        @Volatile
        private var INSTANCE: TourismDatabase? = null
        
        fun getDatabase(context: Context): TourismDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TourismDatabase::class.java,
                    "tourism_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
