package com.voyager.tourism.di

import com.voyager.tourism.data.repository.UserRepositoryImpl
import com.voyager.tourism.data.repository.TripRepositoryImpl
import com.voyager.tourism.domain.repository.UserRepository
import com.voyager.tourism.domain.repository.TripRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository bindings
 * Binds repository implementations to their interfaces
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
    
    @Binds
    @Singleton
    abstract fun bindTripRepository(
        tripRepositoryImpl: TripRepositoryImpl
    ): TripRepository
}
