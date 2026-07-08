package com.dukkan.brands.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.brands.uiState.BrandsUiState
import com.dukkan.domain.model.Brand
import com.dukkan.domain.usecase.GetBrandsUseCase
import com.dukkan.domain.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrandsViewModel @Inject constructor(
    private val getBrandsUseCase: GetBrandsUseCase,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true
        )

    private val _brandsState = MutableStateFlow<BrandsUiState>(BrandsUiState.Loading)
    val brandsState: StateFlow<BrandsUiState> = _brandsState

    init {
        loadBrands()
    }

    fun loadBrands() {
        viewModelScope.launch {
            _brandsState.value = BrandsUiState.Loading
            getBrandsUseCase()
                .catch { e ->
                    _brandsState.value = BrandsUiState.Error(e.message ?: "Unknown Error")
                }
                .collect { brands ->
                    _brandsState.value = BrandsUiState.Success(brands)
                }
        }
    }
}
