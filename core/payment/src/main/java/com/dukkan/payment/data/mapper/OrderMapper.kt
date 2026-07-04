package com.dukkan.payment.data.mapper

import com.dukkan.payment.data.remote.dto.IntentionResponseDto
import com.dukkan.payment.data.remote.dto.OrderConfirmationDto
import com.dukkan.payment.domain.model.PaymentIntentionResult
import com.msayeh.domain.model.Money
import com.msayeh.domain.model.OrderConfirmation
import java.math.BigDecimal

internal fun OrderConfirmationDto.toDomainModel(): OrderConfirmation = OrderConfirmation(
    orderId = orderId,
    status  = status,
    total   = Money(
        amount       = BigDecimal.valueOf(total),
        currencyCode = currency,
    ),
)

internal fun IntentionResponseDto.toDomainModel(): PaymentIntentionResult = PaymentIntentionResult(
    orderId      = orderId,
    clientSecret = clientSecret,
    publicKey    = publicKey,
)
