package com.voyager.tourism.di

import com.voyager.tourism.data.repository.UserRepositoryImpl
import com.voyager.tourism.data.repository.TripRepositoryImpl
import com.voyager.tourism.data.repository.TravelPreferencesRepositoryImpl
import com.voyager.tourism.data.repository.BehaviorAnalysisRepositoryImpl
import com.voyager.tourism.data.repository.AuthRepositoryImpl
import com.voyager.tourism.data.repository.TravelRepositoryImpl
import com.voyager.tourism.domain.repository.UserRepository
import com.voyager.tourism.domain.repository.TripRepository
import com.voyager.tourism.domain.repository.TravelPreferencesRepository
import com.voyager.tourism.domain.repository.BehaviorAnalysisRepository
import com.voyager.tourism.domain.repository.AuthRepository
import com.voyager.tourism.domain.repository.TravelRepository
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
    @Binds
    @Singleton
    abstract fun bindTravelPreferencesRepository(
        impl: TravelPreferencesRepositoryImpl
    ): TravelPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindBehaviorAnalysisRepository(
        impl: BehaviorAnalysisRepositoryImpl
    ): BehaviorAnalysisRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
    
    @Binds
    @Singleton
    abstract fun bindTravelRepository(
        travelRepositoryImpl: TravelRepositoryImpl
    ): TravelRepository
}
