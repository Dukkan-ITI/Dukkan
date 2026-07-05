package com.dukkan.payment.data.mapper

import com.apollographql.apollo.api.Optional
import com.dukkan.domain.model.Address
import com.dukkan.domain.model.Money
import com.dukkan.payment.admin.OrderCreateMutation
import com.dukkan.payment.admin.OrderMarkAsPaidMutation
import com.dukkan.payment.admin.type.CurrencyCode
import com.dukkan.payment.admin.type.MailingAddressInput
import com.dukkan.payment.admin.type.MoneyBagInput
import com.dukkan.payment.admin.type.MoneyInput
import com.dukkan.payment.admin.type.OrderCancelReason
import com.dukkan.payment.admin.type.OrderCreateFinancialStatus
import com.dukkan.payment.admin.type.OrderCreateLineItemInput
import com.dukkan.payment.admin.type.CountryCode
import com.dukkan.payment.admin.type.OrderCreateCustomerInput
import com.dukkan.payment.admin.type.OrderCreateAssociateCustomerAttributesInput
import com.dukkan.payment.admin.type.OrderCreateOrderInput
import com.dukkan.payment.admin.type.OrderCreateOptionsInput
import com.dukkan.payment.admin.type.OrderCreateShippingLineInput
import com.dukkan.payment.admin.type.OrderMarkAsPaidInput
import com.dukkan.payment.domain.model.CreatedOrder
import com.dukkan.payment.domain.model.OrderDraft
import com.dukkan.payment.domain.model.OrderFinancialStatus
import java.math.BigDecimal

internal fun OrderDraft.toAdminInput(status: OrderFinancialStatus): OrderCreateOrderInput =
    OrderCreateOrderInput(
        customer = Optional.present(OrderCreateCustomerInput(toAssociate = Optional.present(OrderCreateAssociateCustomerAttributesInput(id = Optional.present(customerId))))),
        currency = Optional.present(CurrencyCode.safeValueOf(currency)),
        financialStatus = Optional.present(status.toAdminFinancialStatus()),
        lineItems = Optional.present(
            lineItems.map {
                OrderCreateLineItemInput(
                    variantId = Optional.present(it.variantId),
                    quantity = it.quantity,
                )
            }
        ),
        shippingAddress = Optional.presentIfNotNull(shippingAddress?.toMailingAddressInput()),
        shippingLines = Optional.present(
            listOf(
                OrderCreateShippingLineInput(
                    title = shippingLine.title,
                    code = Optional.present(shippingLine.code),
                    priceSet = shippingLine.price.toMoneyBagInput(currency),
                )
            )
        ),
    )

internal fun defaultOrderCreateOptions(): OrderCreateOptionsInput = OrderCreateOptionsInput()

internal fun String.toMarkAsPaidInput(): OrderMarkAsPaidInput = OrderMarkAsPaidInput(id = this)

internal fun OrderFinancialStatus.toAdminFinancialStatus(): OrderCreateFinancialStatus =
    when (this) {
        OrderFinancialStatus.PENDING -> OrderCreateFinancialStatus.PENDING
        OrderFinancialStatus.PAID -> OrderCreateFinancialStatus.PAID
    }

internal fun com.dukkan.payment.domain.model.OrderCancelReason.toAdminCancelReason(): OrderCancelReason =
    when (this) {
        com.dukkan.payment.domain.model.OrderCancelReason.CUSTOMER -> OrderCancelReason.CUSTOMER
        com.dukkan.payment.domain.model.OrderCancelReason.DECLINED -> OrderCancelReason.DECLINED
        com.dukkan.payment.domain.model.OrderCancelReason.FRAUD -> OrderCancelReason.FRAUD
        com.dukkan.payment.domain.model.OrderCancelReason.INVENTORY -> OrderCancelReason.INVENTORY
        com.dukkan.payment.domain.model.OrderCancelReason.OTHER -> OrderCancelReason.OTHER
        com.dukkan.payment.domain.model.OrderCancelReason.STAFF -> OrderCancelReason.STAFF
    }

internal fun OrderCreateMutation.Order.toDomain(): CreatedOrder =
    CreatedOrder(
        orderId = id,
        financialStatus = displayFinancialStatus?.rawValue ?: "",
        total = currentTotalPriceSet.shopMoney.toDomainMoney(),
    )

internal fun OrderMarkAsPaidMutation.Order.toDomain(): CreatedOrder =
    CreatedOrder(
        orderId = id,
        financialStatus = displayFinancialStatus?.rawValue ?: "",
        total = currentTotalPriceSet?.shopMoney?.toDomainMoney() ?: Money(BigDecimal.ZERO, "EGP"),
    )

private fun Money.toMoneyBagInput(currency: String): MoneyBagInput {
    // ponytail: Shopify order currency follows the cart even though Paymob charges EGP in this app-only flow.
    val money = MoneyInput(
        amount = amount.toPlainString(),
        currencyCode = CurrencyCode.safeValueOf(currency),
    )
    return MoneyBagInput(shopMoney = money)
}

private fun Address.toMailingAddressInput(): MailingAddressInput =
    MailingAddressInput(
        firstName = Optional.presentIfNotNull(firstName),
        lastName = Optional.presentIfNotNull(lastName),
        company = Optional.presentIfNotNull(company),
        address1 = Optional.presentIfNotNull(address1),
        address2 = Optional.presentIfNotNull(address2),
        city = Optional.presentIfNotNull(city),
        provinceCode = Optional.presentIfNotNull(province),
        countryCode = Optional.presentIfNotNull(
            country?.let { CountryCode.safeValueOf(it) }?.takeIf { it != CountryCode.UNKNOWN__ }
        ),
        zip = Optional.presentIfNotNull(zip),
        phone = Optional.presentIfNotNull(phone),
    )

private fun OrderCreateMutation.ShopMoney.toDomainMoney(): Money =
    Money(amount = BigDecimal(amount.toString()), currencyCode = currencyCode.rawValue)

private fun OrderMarkAsPaidMutation.ShopMoney.toDomainMoney(): Money =
    Money(amount = BigDecimal(amount.toString()), currencyCode = currencyCode.rawValue)
