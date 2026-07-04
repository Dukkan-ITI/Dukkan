package com.dukkan.shopping_cart.uistate

import com.dukkan.domain.model.cart.CartLine
import com.dukkan.domain.model.cart.StoreCart

data class ShoppingCartState(
    val cart: StoreCart? = null,
    val isLoading: Boolean = false,
    val promoCode: String = "",
    val isApplyingPromo: Boolean = false,
    val promoError: String? = null,
    val showRemoveDialogForItem: CartLine? = null
)
