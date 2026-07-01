package com.msayeh.domain.usecase.cart

import javax.inject.Inject

class CartUseCases @Inject constructor(
    val getCartItems: GetCartItemsUseCase,
    val addToCart: AddToCartUseCase,
    val updateCartQuantity: UpdateCartQuantityUseCase,
    val removeFromCart: RemoveFromCartUseCase,
    val calculateCartTotals: CalculateCartTotalsUseCase
)
