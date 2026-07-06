package com.dukkan.home.uistate

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dukkan.domain.model.Product

sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    class StringResource(@StringRes val resId: Int, vararg val args: Any) : UiText()

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, *args)
        }
    }
}

sealed interface AllProductsUiState {
    object Loading : AllProductsUiState

    data class Success(
        val products: List<Product>,
        val favoriteIds: Set<String>
    ) : AllProductsUiState

    data class Error(val message: UiText) : AllProductsUiState
}
