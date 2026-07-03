package com.dukkan.data.mapper

import com.dukkan.GetCartQuery
import com.dukkan.fragment.MoneyFields
import com.msayeh.domain.model.Money
import com.msayeh.domain.model.NetworkImage
import com.msayeh.domain.model.ProductSummary
import com.msayeh.domain.model.ProductVariant
import com.msayeh.domain.model.cart.*

fun GetCartQuery.Cart.toDomainModel(): StoreCart = StoreCart(
    id = id,
    checkoutUrl = checkoutUrl.toString(),
    totalQuantity = totalQuantity,
    discountCodes = discountCodes.map { it.toDomainModel() },
    cost = cost.toDomainModel(),
    lines = lines.edges.map { it.node.toDomainModel() }
)

private fun GetCartQuery.DiscountCode.toDomainModel(): DiscountCode = DiscountCode(
    code = code,
    applicable = applicable
)

private fun GetCartQuery.Cost.toDomainModel(): CartCost = CartCost(
    subtotalAmount = subtotalAmount.moneyFields.toMoney(),
    totalAmount = totalAmount.moneyFields.toMoney(),
    totalTaxAmount = totalTaxAmount?.moneyFields?.toMoney(),
    checkoutChargeAmount = checkoutChargeAmount.moneyFields.toMoney()
)

private fun GetCartQuery.Node.toDomainModel(): CartLine {
    val variantFragment = merchandise.onProductVariant
        ?: error("Unsupported merchandise type for cart line $id")

    return CartLine(
        id = id,
        quantity = maxOf(quantity, 1),
        cost = cost.toDomainModel(),
        merchandise = variantFragment.toDomainModel()
    )
}

private fun GetCartQuery.Cost1.toDomainModel(): CartLineCost = CartLineCost(
    totalAmount = totalAmount.moneyFields.toMoney(),
    amountPerQuantity = amountPerQuantity.moneyFields.toMoney(),
    compareAtAmountPerQuantity = compareAtAmountPerQuantity?.moneyFields?.toMoney()
)

private fun GetCartQuery.OnProductVariant.toDomainModel(): ProductVariant = ProductVariant(
    id = id,
    title = title,
    price = price.moneyFields.toMoney(),
    compareAtPrice = compareAtPrice?.moneyFields?.toMoney(),
    availableForSale = availableForSale,
    quantityAvailable = quantityAvailable,
    selectedOptions = selectedOptions.map { SelectedOption(name = it.name, value = it.value) },
    image = image?.let { NetworkImage(
        url = it.url.toString(), altText = it.altText,
        blurredUrl = null
    ) },
    product = ProductSummary(
        id = product.id,
        title = product.title,
        vendor = product.vendor
    )
)

private fun MoneyFields.toMoney(): Money = Money.from(amount as String, currencyCode.rawValue)
