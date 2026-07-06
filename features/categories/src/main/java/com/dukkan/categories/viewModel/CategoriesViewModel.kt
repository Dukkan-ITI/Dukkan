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
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase()
                .catch {
                    it.printStackTrace()
                    _categoriesState.value = emptyList()
                }
                .collect { categories ->
                    _categoriesState.value = categories
                }
        }
    }
}