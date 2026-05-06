package com.voyager.tourism.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.voyager.tourism.BuildConfig
import com.voyager.tourism.data.api.AiTravelPreferencesApi
import com.voyager.tourism.data.api.BehaviorAnalysisApi
import com.voyager.tourism.data.api.UserApiService
import com.voyager.tourism.data.api.GoogleAuthApiService
import com.voyager.tourism.data.api.TravelPlanApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.voyager.tourism.data.interceptor.AuthInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Hilt module for network dependencies
 * Provides Retrofit, OkHttpClient, and API service instances
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (false) { // TODO: Change to BuildConfig.DEBUG when available
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            )
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/") // Local backend URL for testing
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    /** Voyager AI microservice (questionnaire, recommendations, etc.) */
    @Provides
    @Singleton
    @Named("ai")
    fun provideAiRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.AI_SERVICE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideGoogleAuthApiService(retrofit: Retrofit): GoogleAuthApiService {
        return retrofit.create(GoogleAuthApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideTravelPlanApiService(retrofit: Retrofit): TravelPlanApiService {
        return retrofit.create(TravelPlanApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAiTravelPreferencesApi(@Named("ai") retrofit: Retrofit): AiTravelPreferencesApi {
        return retrofit.create(AiTravelPreferencesApi::class.java)
    }

    @Provides
    @Singleton
    fun provideBehaviorAnalysisApi(@Named("ai") retrofit: Retrofit): BehaviorAnalysisApi {
        return retrofit.create(BehaviorAnalysisApi::class.java)
    }
}
