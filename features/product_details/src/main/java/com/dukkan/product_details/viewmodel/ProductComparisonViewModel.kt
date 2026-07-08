package com.dukkan.product_details.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.domain.model.ProductComparisonResult
import com.dukkan.domain.repository.ProductsRepository
import com.dukkan.domain.usecase.compare.ProductComparisonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductComparisonState(
    val isLoading: Boolean = true,
    val result: ProductComparisonResult? = null,
    val error: String? = null,
    val answerInput: String = ""
)

@HiltViewModel
class ProductComparisonViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productsRepository: ProductsRepository,
    private val productComparisonUseCase: ProductComparisonUseCase
) : ViewModel() {

    private val productId1: String = checkNotNull(savedStateHandle["productId1"])
    private val productId2: String = checkNotNull(savedStateHandle["productId2"])

    private val _state = MutableStateFlow(ProductComparisonState())
    val state: StateFlow<ProductComparisonState> = _state.asStateFlow()

    private var currentSessionId: String? = null

    init {
        startComparison()
    }

    private fun startComparison(answer: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val p1Result = productsRepository.getProductById(productId1)
            val p2Result = productsRepository.getProductById(productId2)

            if (p1Result.isFailure || p2Result.isFailure) {
                _state.update { it.copy(isLoading = false, error = "Failed to load product details") }
                return@launch
            }

            val p1 = p1Result.getOrThrow()
            val p2 = p2Result.getOrThrow()

            productComparisonUseCase(p1, p2, currentSessionId, answer)
                .onSuccess { result ->
                    if (result is ProductComparisonResult.NeedsMoreInfo) {
                        currentSessionId = result.sessionId
                    }
                    _state.update { it.copy(isLoading = false, result = result) }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Comparison failed") }
                }
        }
    }

    fun onAnswerInputChanged(answer: String) {
        _state.update { it.copy(answerInput = answer) }
    }

    fun submitAnswer() {
        val answer = _state.value.answerInput
        if (answer.isNotBlank()) {
            startComparison(answer)
            _state.update { it.copy(answerInput = "") }
        }
    }

    fun retry() {
        startComparison()
    }
}
