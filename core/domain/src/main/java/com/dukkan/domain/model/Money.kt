package com.dukkan.domain.model

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency

data class Money(
    val amount: BigDecimal,
    val currencyCode: String

) {
    companion object {
        fun from(amount: String, currencyCode: String) =
            Money(BigDecimal(amount), currencyCode)
    }

    fun asString(): String {
        val formatter = NumberFormat.getCurrencyInstance()
        formatter.currency = Currency.getInstance(currencyCode)
        return formatter.format(amount)
    }
}
