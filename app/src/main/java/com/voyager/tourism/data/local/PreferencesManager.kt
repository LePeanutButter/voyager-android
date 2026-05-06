package com.voyager.tourism.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SharedPreferences-backed session store (tokens, current user id, user JSON).
 *
 * Keys must stay in sync with [TokenManager] so HTTP interceptors see the same JWT.
 */
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _authTokenFlow = MutableStateFlow(prefs.getString(KEY_AUTH_TOKEN, null))

    /** Observable access token; mirrors [getAuthToken] after writes. */
    val authTokenFlow: StateFlow<String?> = _authTokenFlow.asStateFlow()

    /** Returns the persisted access token, or `null`. */
    fun getAuthToken(): String? = prefs.getString(KEY_AUTH_TOKEN, null)

    /**
     * Stores the access token and publishes it to [authTokenFlow].
     *
     * @param token Non-blank JWT or bearer token string.
     */
    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
        _authTokenFlow.value = token
    }

    /**
     * Persists or removes the refresh token.
     *
     * @param token Refresh token string, or `null`/blank to delete the key.
     */
    fun saveRefreshToken(token: String?) {
        if (token.isNullOrBlank()) {
            prefs.edit().remove(KEY_REFRESH_TOKEN).apply()
        } else {
            prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
        }
    }

    /** Returns the stored refresh token, or `null`. */
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    /**
     * Persists the backend user id for the active session, or removes it when `null`/blank.
     *
     * @param userId String id from the API, or `null` to clear.
     */
    fun saveCurrentUserId(userId: String?) {
        if (userId.isNullOrBlank()) {
            prefs.edit().remove(KEY_USER_ID).apply()
        } else {
            prefs.edit().putString(KEY_USER_ID, userId).apply()
        }
    }

    /** Returns the persisted current user id, or `null`. */
    fun getCurrentUserId(): String? = prefs.getString(KEY_USER_ID, null)

    /**
     * Persists raw user JSON, or removes the key when `null`/blank.
     *
     * @param json Moshi-serialized [com.voyager.tourism.data.dto.UserDto] or compatible payload.
     */
    fun saveUserJson(json: String?) {
        if (json.isNullOrBlank()) {
            prefs.edit().remove(KEY_USER_JSON).apply()
        } else {
            prefs.edit().putString(KEY_USER_JSON, json).apply()
        }
    }

    /** Returns stored user JSON, or `null`. */
    fun getUserJson(): String? = prefs.getString(KEY_USER_JSON, null)

    /** Removes auth token, refresh token, user id, and user JSON; resets [authTokenFlow] to `null`. */
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
