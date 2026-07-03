package com.dukkan.domain.model

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency

data class Money(
    val amount: BigDecimal = 0.toBigDecimal(),
    val currencyCode: String
) {
    override fun toString(): String {
        return asString()
    }
}

fun Money.asString(): String {
    val formatter = NumberFormat.getCurrencyInstance()
    formatter.currency = Currency.getInstance(currencyCode)
    return formatter.format(amount)
}

operator fun Money.plus(money: Money): Money {
    require(currencyCode == money.currencyCode) { "Cannot add Money with different currency codes" }
    return Money(amount + money.amount, currencyCode)
}

operator fun Money.times(quantity: Int): Money {
    return Money(amount * quantity.toBigDecimal(), currencyCode)
}

fun Money.isZero(): Boolean = amount.signum() == 0