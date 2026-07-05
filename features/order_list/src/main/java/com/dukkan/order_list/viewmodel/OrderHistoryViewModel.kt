package com.dukkan.order_list.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.dukkan.order_list.R
import com.dukkan.order_list.uistate.OrderHistoryUIState
import com.dukkan.domain.usecase.order.GetAllOrdersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val getAllOrdersUseCase: GetAllOrdersUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderHistoryUIState())
    val uiState: StateFlow<OrderHistoryUIState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getAllOrdersUseCase(after = null)
                .onSuccess { page ->
                    _uiState.update {
                        it.copy(
                            orders = page.orders,
                            hasNextPage = page.hasNextPage,
                            endCursor = page.endCursor,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            error = error.message
                                ?: context.getString(R.string.error_failed_to_load_orders),
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun loadMoreOrders() {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isPaginating || !currentState.hasNextPage) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPaginating = true) }
            getAllOrdersUseCase(after = currentState.endCursor)
                .onSuccess { page ->
                    _uiState.update {
                        it.copy(
                            orders = it.orders + page.orders,
                            hasNextPage = page.hasNextPage,
                            endCursor = page.endCursor,
                            isPaginating = false
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isPaginating = false) }
                }
        }
    }
}
