package com.msayeh.domain.usecase.cart

import com.msayeh.domain.model.CartItem
import javax.inject.Inject

data class CartTotals(
    val subtotal: Double,
    val shipping: Double,
    val total: Double
)

class CalculateCartTotalsUseCase @Inject constructor() {
    operator fun invoke(items: List<CartItem>, shippingCost: Double = 6.0): CartTotals {
        val subtotal = items.sumOf { (it.price.toDoubleOrNull() ?: 0.0) * it.quantity }
        val finalShipping = if (items.isEmpty()) 0.0 else shippingCost
        val total = subtotal + finalShipping

        return CartTotals(
            subtotal = subtotal,
            shipping = finalShipping,
            total = total
        )
    }
}
