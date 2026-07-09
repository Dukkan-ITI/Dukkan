package com.dukkan.categories.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.categories.uiState.CategoriesUiState
import com.dukkan.domain.model.Category.Category
import com.dukkan.domain.usecase.category.GetCategoriesUseCase
import com.dukkan.domain.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true
        )

    private val _categoriesState = MutableStateFlow<CategoriesUiState>(CategoriesUiState.Loading)
    val categoriesState: StateFlow<CategoriesUiState> = _categoriesState

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _categoriesState.value = CategoriesUiState.Loading
            getCategoriesUseCase()
                .catch { e ->
                    _categoriesState.value = CategoriesUiState.Error(e.message ?: "Unknown Error")
                }
                .collect { categories ->
                    _categoriesState.value = CategoriesUiState.Success(categories)
                }
        }
    }
}
