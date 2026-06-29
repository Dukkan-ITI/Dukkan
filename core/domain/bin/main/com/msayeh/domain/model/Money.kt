package com.msayeh.domain.model

import java.math.BigDecimal

data class Money(
    val amount: BigDecimal,
    val currencyCode: String
)
