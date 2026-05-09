package com.voyager.tourism.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.voyager.tourism.BuildConfig
import com.voyager.tourism.data.api.AiTravelPreferencesApi
import com.voyager.tourism.data.api.BackendMiscApiService
import com.voyager.tourism.data.api.BehaviorAnalysisApi
import com.voyager.tourism.data.api.CatalogApiService
import com.voyager.tourism.data.api.GoogleAuthApiService
import com.voyager.tourism.data.api.SocialApiService
import com.voyager.tourism.data.api.TravelApiService
import com.voyager.tourism.data.api.TravelPlanApiService
import com.voyager.tourism.data.api.UserApiService
import com.voyager.tourism.data.api.VoyagerAiApi
import com.voyager.tourism.data.interceptor.AuthInterceptor
import com.voyager.tourism.data.interceptor.UnauthorizedResponseInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Dagger Hilt module that wires Retrofit, Moshi, and OkHttp for **voyager-backend-core**
 * (`BACKEND_BASE_URL`, must end with `/api/v1/`) and **voyager-ai-service**
 * (`AI_SERVICE_BASE_URL`, debe terminar en barra api v1). Expone VoyagerAiApi: local recommendations, tendencias, matching, chat, etc.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private fun newOkHttpClient(
        readTimeoutSec: Long,
        writeTimeoutSec: Long,
        authInterceptor: AuthInterceptor,
        unauthorizedResponseInterceptor: UnauthorizedResponseInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                },
            )
            .addInterceptor(authInterceptor)
            .addInterceptor(unauthorizedResponseInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(readTimeoutSec, TimeUnit.SECONDS)
            .writeTimeout(writeTimeoutSec, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provides a Moshi instance with Kotlin JSON adapter support for Retrofit.
     */
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        unauthorizedResponseInterceptor: UnauthorizedResponseInterceptor,
    ): OkHttpClient {
        return newOkHttpClient(
            readTimeoutSec = 60,
            writeTimeoutSec = 60,
            authInterceptor = authInterceptor,
            unauthorizedResponseInterceptor = unauthorizedResponseInterceptor,
        )
    }

    /**
     * Cliente HTTP para el microservicio IA: lecturas largas (Ollama) como en `aiMicroservice.js` del web.
     */
    @Provides
    @Singleton
    @Named("ai_client")
    fun provideAiOkHttpClient(
        authInterceptor: AuthInterceptor,
        unauthorizedResponseInterceptor: UnauthorizedResponseInterceptor,
    ): OkHttpClient {
        return newOkHttpClient(
            readTimeoutSec = 180,
            writeTimeoutSec = 120,
            authInterceptor = authInterceptor,
            unauthorizedResponseInterceptor = unauthorizedResponseInterceptor,
        )
    }

    /**
     * Provides a [Retrofit] client configured with [BuildConfig.BACKEND_BASE_URL] and Moshi converters.
     */
    @Provides
    @Singleton
    fun provideBackendRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    /**
     * Provides a [Retrofit] client for the AI service using [BuildConfig.AI_SERVICE_BASE_URL].
     */
    @Provides
    @Singleton
    @Named("ai")
    fun provideAiRetrofit(@Named("ai_client") okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.AI_SERVICE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    /**
     * Provides the backend user API service.
     */
    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService =
        retrofit.create(UserApiService::class.java)

    /**
     * Provides the Google OAuth API service for the backend retrofit instance.
     */
    @Provides
    @Singleton
    fun provideGoogleAuthApiService(retrofit: Retrofit): GoogleAuthApiService =
        retrofit.create(GoogleAuthApiService::class.java)

    /**
     * Provides the travel plan API service.
     */
    @Provides
    @Singleton
    fun provideTravelPlanApiService(retrofit: Retrofit): TravelPlanApiService =
        retrofit.create(TravelPlanApiService::class.java)

    /**
     * Provides the travel catalog and related travel API service.
     */
    @Provides
    @Singleton
    fun provideTravelApiService(retrofit: Retrofit): TravelApiService =
        retrofit.create(TravelApiService::class.java)

    /**
     * Provides the social features API service.
     */
    @Provides
    @Singleton
    fun provideSocialApiService(retrofit: Retrofit): SocialApiService =
        retrofit.create(SocialApiService::class.java)

    /**
     * Provides the catalog API service (destinations, places, etc.).
     */
    @Provides
    @Singleton
    fun provideCatalogApiService(retrofit: Retrofit): CatalogApiService =
        retrofit.create(CatalogApiService::class.java)

    /**
     * Provides miscellaneous backend endpoints not covered by other API facades.
     */
    @Provides
    @Singleton
    fun provideBackendMiscApiService(retrofit: Retrofit): BackendMiscApiService =
        retrofit.create(BackendMiscApiService::class.java)

    /**
     * Provides the AI travel preferences API using the AI-service [Retrofit] instance.
     */
    @Provides
    @Singleton
    fun provideAiTravelPreferencesApi(@Named("ai") retrofit: Retrofit): AiTravelPreferencesApi =
        retrofit.create(AiTravelPreferencesApi::class.java)

    /**
     * Provides the behavior analysis API using the AI-service [Retrofit] instance.
     */
    @Provides
    @Singleton
    fun provideBehaviorAnalysisApi(@Named("ai") retrofit: Retrofit): BehaviorAnalysisApi =
        retrofit.create(BehaviorAnalysisApi::class.java)

    /**
     * Provides the Voyager AI assistant API using the AI-service [Retrofit] instance.
     */
    @Provides
    @Singleton
    fun provideVoyagerAiApi(@Named("ai") retrofit: Retrofit): VoyagerAiApi =
        retrofit.create(VoyagerAiApi::class.java)
}
