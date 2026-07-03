package com.dukkan.shopping_cart.uistate

import com.dukkan.domain.model.CartItem
import com.dukkan.domain.model.Money

data class ShoppingCartState(
    val cartItems: List<CartItem> = emptyList(),
    val subtotal: Money = Money(currencyCode = "USD"),
    val shipping: Money = Money(currencyCode = "USD"),
    val total: Money = Money(currencyCode = "USD"),
    val promoCode: String = "",
    val isApplyingPromo: Boolean = false,
    val promoError: String? = null,
    val showRemoveDialogForItem: CartItem? = null,
    val pricesOutdated: Boolean = false
)
