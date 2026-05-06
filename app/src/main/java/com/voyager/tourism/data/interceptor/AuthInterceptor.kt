package com.voyager.tourism.data.interceptor

import com.voyager.tourism.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * HTTP interceptor that adds JWT token to all API requests
 * Automatically adds Authorization header with Bearer token
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    /**
     * Observes the outgoing request, attaches `Authorization: Bearer <token>` when a JWT is stored,
     * and proceeds with the possibly modified request.
     *
     * @param chain OkHttp chain for the current call.
     * @return The [Response] from [Interceptor.Chain.proceed].
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenManager.getToken()
        
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        
        return chain.proceed(request)
    }
}
