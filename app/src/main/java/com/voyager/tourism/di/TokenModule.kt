package com.voyager.tourism.di

import android.content.Context
import com.voyager.tourism.data.local.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for token management dependencies
 * Provides TokenManager instance for authentication
 */
@Module
@InstallIn(SingletonComponent::class)
object TokenModule {
    
    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }
}
