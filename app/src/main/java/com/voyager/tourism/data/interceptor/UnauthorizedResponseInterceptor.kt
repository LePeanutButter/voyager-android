package com.voyager.tourism.data.interceptor

import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.data.session.SessionInvalidationNotifier
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Clears local session state when the backend returns HTTP **401 Unauthorized**.
 *
 * Invokes [PreferencesManager.clearAuthData] and [SessionInvalidationNotifier.notifySessionExpired]
 * so the app returns to an unauthenticated state and UI can react.
 */
@Singleton
class UnauthorizedResponseInterceptor @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val sessionInvalidationNotifier: SessionInvalidationNotifier,
) : Interceptor {

    /**
     * Executes the request and, if the response code is `401`, wipes auth prefs and notifies listeners.
     *
     * @param chain OkHttp chain for the current call.
     * @return The server [Response] (including `401`), unmodified aside from the chain’s normal behavior.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401) {
            preferencesManager.clearAuthData()
            sessionInvalidationNotifier.notifySessionExpired()
        }
        return response
    }
}
