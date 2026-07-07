package com.dukkan.address.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.domain.model.LocationDetail
import com.dukkan.domain.repository.PlacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlacesViewModel @Inject constructor(
    private val placesRepository: PlacesRepository
) : ViewModel() {

    private val _predictions = MutableStateFlow<List<LocationDetail>>(emptyList())
    val predictions: StateFlow<List<LocationDetail>> = _predictions.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    fun searchPlaces(query: String) {
        searchJob?.cancel()
        if (query.isEmpty()) {
            _predictions.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // debounce
            _isSearching.value = true
            val result = placesRepository.getAutocompletePredictions(query)
            result.onSuccess {
                _predictions.value = it
            }.onFailure {
                _predictions.value = emptyList()
            }
            _isSearching.value = false
        }
    }

    fun clearPredictions() {
        _predictions.value = emptyList()
    }
}
