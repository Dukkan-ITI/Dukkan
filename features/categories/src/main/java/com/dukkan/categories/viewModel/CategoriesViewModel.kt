package com.dukkan.categories.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.domain.model.Category.Category
import com.dukkan.domain.usecase.category.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _categoriesState = MutableStateFlow<List<Category>>(emptyList())
    val categoriesState: StateFlow<List<Category>> = _categoriesState

    init {
        android.util.Log.d("CategoriesDebug", "ViewModel init called")
        viewModelScope.launch {
            getCategoriesUseCase()
                .catch { e -> android.util.Log.e("CategoriesDebug", "Flow error: ${e.message}") }
                .collect { categories ->
                    android.util.Log.d("CategoriesDebug", "Collected ${categories.size} categories")
                    _categoriesState.value = categories
                }
        }
    }
}
