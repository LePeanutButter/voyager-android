package com.voyager.tourism.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Almacenamiento local de sesión (token, usuario actual).
 * Debe usar las mismas claves que [TokenManager] para que el interceptor HTTP reciba el JWT.
 */
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _authTokenFlow = MutableStateFlow(prefs.getString(KEY_AUTH_TOKEN, null))
    val authTokenFlow: StateFlow<String?> = _authTokenFlow.asStateFlow()

    fun getAuthToken(): String? = prefs.getString(KEY_AUTH_TOKEN, null)

    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
        _authTokenFlow.value = token
    }

    fun saveRefreshToken(token: String?) {
        if (token.isNullOrBlank()) {
            prefs.edit().remove(KEY_REFRESH_TOKEN).apply()
        } else {
            prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
        }
    }

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun saveCurrentUserId(userId: String?) {
        if (userId.isNullOrBlank()) {
            prefs.edit().remove(KEY_USER_ID).apply()
        } else {
            prefs.edit().putString(KEY_USER_ID, userId).apply()
        }
    }

    fun getCurrentUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun saveUserJson(json: String?) {
        if (json.isNullOrBlank()) {
            prefs.edit().remove(KEY_USER_JSON).apply()
        } else {
            prefs.edit().putString(KEY_USER_JSON, json).apply()
        }
    }

    fun getUserJson(): String? = prefs.getString(KEY_USER_JSON, null)

    fun clearAuthData() {
        prefs.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_JSON)
            .apply()
        _authTokenFlow.value = null
    }

    companion object {
        const val PREFS_NAME = "voyager_prefs"
        const val KEY_AUTH_TOKEN = "auth_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_USER_ID = "current_user_id"
        const val KEY_USER_JSON = "user_json"
    }
}
