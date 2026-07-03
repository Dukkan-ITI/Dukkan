package com.dukkan.domain.usecase.cart

import com.dukkan.domain.model.CartItem
import com.dukkan.domain.model.Money
import com.dukkan.domain.model.calculateSubtotal
import com.dukkan.domain.model.plus
import javax.inject.Inject

data class CartTotals(
    val subtotal: Money,
    val shipping: Money,
    val total: Money
)

class CalculateCartTotalsUseCase @Inject constructor() {
    operator fun invoke(items: List<CartItem>, shippingCost: Double = 6.0): CartTotals {
        val subtotal = items.calculateSubtotal()
        val finalShipping = if (items.isEmpty()) Money(
            amount = 0.toBigDecimal(),
            currencyCode = subtotal.currencyCode
        ) else Money(amount = shippingCost.toBigDecimal(), currencyCode = subtotal.currencyCode)
        val total = subtotal + finalShipping

        return CartTotals(
            subtotal = subtotal,
            shipping = finalShipping,
            total = total
        )
    }
}
