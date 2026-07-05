package com.dukkan.categories.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.domain.usecase.category.GetProductTypesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val getProductTypesUseCase: GetProductTypesUseCase
) : ViewModel() {

    private val _categoriesState = MutableStateFlow<List<String>>(emptyList())
    val categoriesState: StateFlow<List<String>> = _categoriesState

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                _categoriesState.value = getProductTypesUseCase()
            } catch (e: Exception) {
                e.printStackTrace()
                _categoriesState.value = emptyList()
            }
        }
    }
}