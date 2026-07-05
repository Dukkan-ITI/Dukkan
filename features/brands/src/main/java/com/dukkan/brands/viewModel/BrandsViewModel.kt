package com.dukkan.brands.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.domain.model.Brand
import com.dukkan.domain.usecase.GetBrandsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrandsViewModel @Inject constructor(
    private val getBrandsUseCase: GetBrandsUseCase
) : ViewModel() {

    private val _brandsState = MutableStateFlow<List<Brand>>(emptyList())
    val brandsState: StateFlow<List<Brand>> = _brandsState

    init {
        android.util.Log.d("BrandsDebug", "ViewModel init called")
        viewModelScope.launch {
            getBrandsUseCase()
                .catch { e -> android.util.Log.e("BrandsDebug", "Flow error: ${e.message}") }
                .collect { brands ->
                    android.util.Log.d("BrandsDebug", "Collected ${brands.size} brands")
                    _brandsState.value = brands
                }
        }
    }
}