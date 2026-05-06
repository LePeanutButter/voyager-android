package com.voyager.tourism.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaceDetailViewModel @Inject constructor(
    private val voyagerAiRepository: VoyagerAiRepository,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _payload = MutableStateFlow<String?>(null)
    val payload: StateFlow<String?> = _payload.asStateFlow()

    fun loadPlace(placeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _payload.value = null
            runCatching {
                val response = voyagerAiRepository.getPopularActivities(location = placeId, limit = 10)
                if (response.isSuccessful) {
                    _payload.value = response.body()?.string()?.take(12_000)
                        ?: "(Respuesta vacía)"
                } else {
                    _error.value = response.errorBody()?.string()?.take(2_000)
                        ?: "HTTP ${response.code()}"
                }
            }.onFailure {
                _error.value = it.message ?: "Error de red"
            }
            _isLoading.value = false
        }
    }
}
