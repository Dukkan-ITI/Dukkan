package com.msayeh.domain.model.cart


data class StoreCart(
    val id: String,
    val checkoutUrl: String,
    val totalQuantity: Int,
    val discountCodes: List<DiscountCode>,
    val cost: CartCost,
    val lines: List<CartLine>
) {
    val isEmpty: Boolean get() = lines.isEmpty()
    val appliedDiscounts: List<DiscountCode> get() = discountCodes.filter { it.applicable }
}

data class SelectedOption(
    val name: String,
    val value: String
)