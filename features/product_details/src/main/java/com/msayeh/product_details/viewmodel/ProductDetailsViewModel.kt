package com.msayeh.product_details.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.navigation.Screen
import com.msayeh.domain.usecase.product.GetProductByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProductByIdUseCase: GetProductByIdUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(ProductDetailsState())
    val state = _state.asStateFlow()

    private val productId: String =
        checkNotNull(savedStateHandle[Screen.ProductDetail.ARG_PRODUCT_ID]) {
            "Product ID is required"
        }

    fun getProductDetails() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            getProductByIdUseCase("gid://shopify/Product/$productId").onSuccess { product ->
                _state.value = _state.value.copy(isLoading = false, product = product)
            }.onFailure { error ->
                _state.value = _state.value.copy(isLoading = false, error = error.message)
            }
        }
    }
}