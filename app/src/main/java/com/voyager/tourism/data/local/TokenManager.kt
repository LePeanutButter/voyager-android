package com.voyager.tourism.data.local

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin facade over [PreferencesManager] for the access token and persisted user JSON snapshot.
 *
 * [com.voyager.tourism.data.interceptor.AuthInterceptor] reads the token via this class.
 */
@Singleton
class TokenManager @Inject constructor(
    private val preferencesManager: PreferencesManager,
) {
    /** Returns the current JWT, or `null` if the user is logged out. */
    fun getToken(): String? = preferencesManager.getAuthToken()

    /** Persists the access token and updates the in-memory [PreferencesManager.authTokenFlow]. */
    fun saveToken(token: String) = preferencesManager.saveAuthToken(token)

    /** Persists the raw user JSON (e.g. after login) for offline or bootstrap use. */
    fun saveUser(json: String) = preferencesManager.saveUserJson(json)

    /** Returns the stored user JSON string, or `null`. */
    fun getUser(): String? = preferencesManager.getUserJson()

    /** Returns the current user ID, or `null`. */
    fun getCurrentUserId(): String? = preferencesManager.getCurrentUserId()

    /** Clears token, refresh token, user id, and user JSON from preferences. */
    fun clear() = preferencesManager.clearAuthData()
}
