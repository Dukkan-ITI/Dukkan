package com.dukkan.domain.usecase.cart

import com.dukkan.domain.model.AppCurrency
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
    operator fun invoke(items: List<CartItem>, currency: AppCurrency, shippingCost: Double = 6.0): CartTotals {
        val subtotal = items.calculateSubtotal(currency.code)
        val finalShipping = if (items.isEmpty()) Money(
            amount = 0.toBigDecimal(),
            currencyCode = currency.code
        ) else Money(amount = shippingCost.toBigDecimal(), currencyCode = currency.code)
        val total = subtotal + finalShipping

        return CartTotals(
            subtotal = subtotal,
            shipping = finalShipping,
            total = total
        )
    }
}
