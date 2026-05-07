package com.voyager.tourism.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.rules.ExternalResource
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Starts a [MockWebServer] and exposes a [Retrofit] instance whose `baseUrl` is
 * `http://localhost:<port>/api/v1/` — matching [com.voyager.tourism.di.NetworkModule] layout.
 */
class RetrofitMockWebServerRule : ExternalResource() {

    val server = MockWebServer()

    lateinit var retrofit: Retrofit
        private set

    val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    override fun before() {
        server.start()
        val base = server.url("/api/v1/")
        retrofit = Retrofit.Builder()
            .baseUrl(base)
            .client(
                OkHttpClient.Builder()
                    .build(),
            )
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    override fun after() {
        server.shutdown()
    }
}
