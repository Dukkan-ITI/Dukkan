package com.msayeh.domain.usecase.cart

import javax.inject.Inject

class CartUseCases @Inject constructor(
    val getCart: GetCartUseCase,
    val addToCart: AddToCartUseCase,
    val updateCartQuantity: UpdateCartQuantityUseCase,
    val removeFromCart: RemoveFromCartUseCase,
    val createCart: CreateCartUseCase,
    val saveCartId: SaveCartIdUseCase,
    val applyDiscountCode: ApplyDiscountCodeUseCase
)
