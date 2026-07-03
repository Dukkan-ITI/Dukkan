package com.msayeh.domain.model

import com.msayeh.domain.model.cart.SelectedOption

data class ProductVariant(
    val id: String,
    val title: String,
    val price: Money,
    val compareAtPrice: Money?,
    val availableForSale: Boolean,
    val quantityAvailable: Int?,
    val selectedOptions: List<SelectedOption>,
    val image: NetworkImage?,
    val product: ProductSummary
) {
    val isOnSale: Boolean
        get() = compareAtPrice != null && compareAtPrice.amount > price.amount
}
