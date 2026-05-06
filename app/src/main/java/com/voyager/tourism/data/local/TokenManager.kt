package com.voyager.tourism.data.local

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fachada ligera sobre [PreferencesManager] para token y snapshot de usuario (JSON).
 * [com.voyager.tourism.data.interceptor.AuthInterceptor] lee el token aquí.
 */
@Singleton
class TokenManager @Inject constructor(
    private val preferencesManager: PreferencesManager,
) {
    fun getToken(): String? = preferencesManager.getAuthToken()

    fun saveToken(token: String) = preferencesManager.saveAuthToken(token)

    fun saveUser(json: String) = preferencesManager.saveUserJson(json)

    fun getUser(): String? = preferencesManager.getUserJson()

    fun clear() = preferencesManager.clearAuthData()
}
