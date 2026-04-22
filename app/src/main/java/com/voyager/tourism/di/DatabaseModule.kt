package com.voyager.tourism.di

import android.content.Context
import androidx.room.Room
import com.voyager.tourism.data.database.TourismDatabase
import com.voyager.tourism.data.database.dao.UserDao
import com.voyager.tourism.data.database.dao.TripDao
import com.voyager.tourism.data.database.dao.DestinationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database dependencies
 * Provides Room database and DAO instances
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideTourismDatabase(@ApplicationContext context: Context): TourismDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            TourismDatabase::class.java,
            "tourism_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    fun provideUserDao(database: TourismDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    fun provideTripDao(database: TourismDatabase): TripDao {
        return database.tripDao()
    }
    
    @Provides
    fun provideDestinationDao(database: TourismDatabase): DestinationDao {
        return database.destinationDao()
    }
}
