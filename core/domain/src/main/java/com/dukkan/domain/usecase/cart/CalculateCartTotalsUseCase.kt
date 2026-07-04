package com.msayeh.domain.usecase.cart

import com.msayeh.domain.model.cart.CartLine
import javax.inject.Inject

data class CartTotals(
    val subtotal: Double,
    val shipping: Double,
    val total: Double
)

class CalculateCartTotalsUseCase @Inject constructor() {
    operator fun invoke(lines: List<CartLine>, shippingCost: Double = 6.0): CartTotals {
        val subtotal = lines.sumOf { line ->
            line.cost.totalAmount.amount.toDouble()
        }
        val finalShipping = if (lines.isEmpty()) 0.0 else shippingCost
        val total = subtotal + finalShipping

        return CartTotals(
            subtotal = subtotal,
            shipping = finalShipping,
            total = total
        )
    }
}
