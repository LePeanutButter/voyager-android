package com.voyager.tourism.data.interceptor

import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.data.session.SessionInvalidationNotifier
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnauthorizedResponseInterceptor @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val sessionInvalidationNotifier: SessionInvalidationNotifier,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401) {
            preferencesManager.clearAuthData()
            sessionInvalidationNotifier.notifySessionExpired()
        }
        return response
    }
}
