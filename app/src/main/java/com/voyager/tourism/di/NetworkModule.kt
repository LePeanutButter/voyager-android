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
 * Red hacia **voyager-backend-core** (`BACKEND_BASE_URL`, termina en `/api/v1/`)
 * y **voyager-ai-service** (`AI_SERVICE_BASE_URL`, termina en `/api/v1/`).
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
    fun provideOkHttpClient(
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
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideBackendRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    @Named("ai")
    fun provideAiRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.AI_SERVICE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService =
        retrofit.create(UserApiService::class.java)

    @Provides
    @Singleton
    fun provideGoogleAuthApiService(retrofit: Retrofit): GoogleAuthApiService =
        retrofit.create(GoogleAuthApiService::class.java)

    @Provides
    @Singleton
    fun provideTravelPlanApiService(retrofit: Retrofit): TravelPlanApiService =
        retrofit.create(TravelPlanApiService::class.java)

    @Provides
    @Singleton
    fun provideTravelApiService(retrofit: Retrofit): TravelApiService =
        retrofit.create(TravelApiService::class.java)

    @Provides
    @Singleton
    fun provideSocialApiService(retrofit: Retrofit): SocialApiService =
        retrofit.create(SocialApiService::class.java)

    @Provides
    @Singleton
    fun provideCatalogApiService(retrofit: Retrofit): CatalogApiService =
        retrofit.create(CatalogApiService::class.java)

    @Provides
    @Singleton
    fun provideBackendMiscApiService(retrofit: Retrofit): BackendMiscApiService =
        retrofit.create(BackendMiscApiService::class.java)

    @Provides
    @Singleton
    fun provideAiTravelPreferencesApi(@Named("ai") retrofit: Retrofit): AiTravelPreferencesApi =
        retrofit.create(AiTravelPreferencesApi::class.java)

    @Provides
    @Singleton
    fun provideBehaviorAnalysisApi(@Named("ai") retrofit: Retrofit): BehaviorAnalysisApi =
        retrofit.create(BehaviorAnalysisApi::class.java)

    @Provides
    @Singleton
    fun provideVoyagerAiApi(@Named("ai") retrofit: Retrofit): VoyagerAiApi =
        retrofit.create(VoyagerAiApi::class.java)
}
