package com.voyager.tourism.data.local

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.voyager.tourism.data.dto.SmarTripSettingsPayload
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SharedPreferences-backed session store (tokens, current user id, user JSON).
 *
 * Keys must stay in sync with [TokenManager] so HTTP interceptors see the same JWT.
 * Ajustes locales de producto: [KEY_SMARTRIP_SETTINGS], alineado con el web (`smartrip_settings`).
 */
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val moshi: Moshi,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _authTokenFlow = MutableStateFlow(prefs.getString(KEY_AUTH_TOKEN, null))

    private val _darkThemeFlow = MutableStateFlow(false)

    /** Refleja [SmarTripSettingsPayload.darkMode] para [com.voyager.tourism.presentation.ui.theme.SmarTripTheme]. */
    val darkThemeFlow: StateFlow<Boolean> = _darkThemeFlow.asStateFlow()

    init {
        _darkThemeFlow.value = getSmarTripSettings().darkMode
    }

    /** Para tests sin Hilt (Moshi mínimo). */
    constructor(context: Context) : this(
        context,
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build(),
    )

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
     * Sesión de chat IA local (misma idea que `localAiSession.js` en el web: una por usuario).
     */
    fun getOrCreateLocalChatSessionId(userId: String): String {
        val key = localAiSessionKey(userId)
        var sid = prefs.getString(key, null)
        if (sid.isNullOrBlank()) {
            sid = UUID.randomUUID().toString()
            prefs.edit().putString(key, sid).apply()
        }
        return sid
    }

    /** Nueva sesión (equivale a `rotateLocalChatSessionId` en el web). */
    fun rotateLocalChatSessionId(userId: String): String {
        val sid = UUID.randomUUID().toString()
        prefs.edit().putString(localAiSessionKey(userId), sid).apply()
        return sid
    }

    private fun localAiSessionKey(userId: String): String = "${KEY_LOCAL_AI_SESSION_PREFIX}$userId"

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

    /** Lee ajustes locales SmarTrip (misma clave JSON que el cliente web). */
    fun getSmarTripSettings(): SmarTripSettingsPayload {
        val raw = prefs.getString(KEY_SMARTRIP_SETTINGS, null) ?: return SmarTripSettingsPayload()
        return try {
            moshi.adapter(SmarTripSettingsPayload::class.java).fromJson(raw) ?: SmarTripSettingsPayload()
        } catch (_: Exception) {
            SmarTripSettingsPayload()
        }
    }

    /** Persiste el objeto completo y actualiza el flujo de tema. */
    fun saveSmarTripSettings(settings: SmarTripSettingsPayload) {
        val json = moshi.adapter(SmarTripSettingsPayload::class.java).toJson(settings)
        prefs.edit().putString(KEY_SMARTRIP_SETTINGS, json).apply()
        _darkThemeFlow.value = settings.darkMode
    }

    fun updateSmarTripSettings(transform: (SmarTripSettingsPayload) -> SmarTripSettingsPayload) {
        saveSmarTripSettings(transform(getSmarTripSettings()))
    }

    /** Removes auth token, refresh token, user id, and user JSON; resets [authTokenFlow] to `null`. */
    fun clearAuthData() {
        val uid = prefs.getString(KEY_USER_ID, null)
        val editor = prefs.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_JSON)
        if (!uid.isNullOrBlank()) {
            editor.remove(localAiSessionKey(uid))
        }
        editor.apply()
        _authTokenFlow.value = null
    }

    companion object {
        const val PREFS_NAME = "voyager_prefs"
        const val KEY_AUTH_TOKEN = "auth_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_USER_ID = "current_user_id"
        const val KEY_USER_JSON = "user_json"
        const val KEY_SMARTRIP_SETTINGS = "smartrip_settings"
        internal const val KEY_LOCAL_AI_SESSION_PREFIX = "local_ai_session:"
    }
}
