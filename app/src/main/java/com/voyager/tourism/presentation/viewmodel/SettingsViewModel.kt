package com.voyager.tourism.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.voyager.tourism.data.dto.SmarTripSettingsPayload
import com.voyager.tourism.data.local.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Estado de ajustes locales alineado con [voyager-web-client/src/pages/Settings/SettingsPage.jsx].
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _state = MutableStateFlow(preferencesManager.getSmarTripSettings())
    val state: StateFlow<SmarTripSettingsPayload> = _state.asStateFlow()

    fun refresh() {
        _state.value = preferencesManager.getSmarTripSettings()
    }

    fun setDarkMode(enabled: Boolean) {
        preferencesManager.updateSmarTripSettings { it.copy(darkMode = enabled) }
        _state.value = preferencesManager.getSmarTripSettings()
    }

    fun setCommunitySuggestions(enabled: Boolean) {
        preferencesManager.updateSmarTripSettings { it.copy(communitySuggestions = enabled) }
        _state.value = preferencesManager.getSmarTripSettings()
    }

    fun setProfileVisibility(value: String) {
        preferencesManager.updateSmarTripSettings { it.copy(profileVisibility = value) }
        _state.value = preferencesManager.getSmarTripSettings()
    }
}
