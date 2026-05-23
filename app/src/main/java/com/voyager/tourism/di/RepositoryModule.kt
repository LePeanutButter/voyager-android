package com.voyager.tourism.di

import com.voyager.tourism.data.repository.UserRepositoryImpl
import com.voyager.tourism.data.repository.TripRepositoryImpl
import com.voyager.tourism.data.repository.SocialRepositoryImpl
import com.voyager.tourism.data.repository.TravelPreferencesRepositoryImpl
import com.voyager.tourism.data.repository.BehaviorAnalysisRepositoryImpl
import com.voyager.tourism.data.repository.AuthRepositoryImpl
import com.voyager.tourism.data.repository.TravelRepositoryImpl
import com.voyager.tourism.data.repository.VoyagerAiRepositoryImpl
import com.voyager.tourism.data.repository.CatalogRepositoryImpl
import com.voyager.tourism.data.repository.BackendSupplementRepositoryImpl
import com.voyager.tourism.domain.repository.UserRepository
import com.voyager.tourism.domain.repository.TripRepository
import com.voyager.tourism.domain.repository.SocialRepository
import com.voyager.tourism.domain.repository.TravelPreferencesRepository
import com.voyager.tourism.domain.repository.BehaviorAnalysisRepository
import com.voyager.tourism.domain.repository.AuthRepository
import com.voyager.tourism.domain.repository.TravelRepository
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.domain.repository.CatalogRepository
import com.voyager.tourism.domain.repository.BackendSupplementRepository
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
    
    /**
     * Binds the user repository implementation to [UserRepository].
     */
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
    
    /**
     * Binds the trip repository implementation to [TripRepository].
     */
    @Binds
    @Singleton
    abstract fun bindTripRepository(
        tripRepositoryImpl: TripRepositoryImpl
    ): TripRepository

    /**
     * Binds the social repository implementation to [SocialRepository].
     */
    @Binds
    @Singleton
    abstract fun bindSocialRepository(
        socialRepositoryImpl: SocialRepositoryImpl
    ): SocialRepository

    /**
     * Binds the travel preferences repository implementation to [TravelPreferencesRepository].
     */
    @Binds
    @Singleton
    abstract fun bindTravelPreferencesRepository(
        impl: TravelPreferencesRepositoryImpl
    ): TravelPreferencesRepository

    /**
     * Binds the behavior analysis repository implementation to [BehaviorAnalysisRepository].
     */
    @Binds
    @Singleton
    abstract fun bindBehaviorAnalysisRepository(
        impl: BehaviorAnalysisRepositoryImpl
    ): BehaviorAnalysisRepository

    /**
     * Binds the auth repository implementation to [AuthRepository].
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
    
    /**
     * Binds the travel repository implementation to [TravelRepository].
     */
    @Binds
    @Singleton
    abstract fun bindTravelRepository(
        travelRepositoryImpl: TravelRepositoryImpl
    ): TravelRepository

    /**
     * Binds the Voyager AI repository implementation to [VoyagerAiRepository].
     */
    @Binds
    @Singleton
    abstract fun bindVoyagerAiRepository(
        impl: VoyagerAiRepositoryImpl
    ): VoyagerAiRepository

    /**
     * Binds the catalog repository implementation to [CatalogRepository].
     */
    @Binds
    @Singleton
    abstract fun bindCatalogRepository(
        impl: CatalogRepositoryImpl
    ): CatalogRepository

    /**
     * Binds the backend supplement repository implementation to [BackendSupplementRepository].
     */
    @Binds
    @Singleton
    abstract fun bindBackendSupplementRepository(
        impl: BackendSupplementRepositoryImpl
    ): BackendSupplementRepository
}
