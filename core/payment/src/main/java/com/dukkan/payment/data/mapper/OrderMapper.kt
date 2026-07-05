package com.dukkan.payment.data.mapper

import com.dukkan.payment.data.remote.dto.PaymobIntentionResponse
import com.dukkan.payment.domain.model.PaymentIntentionResult

internal fun PaymobIntentionResponse.toDomainModel(orderId: String): PaymentIntentionResult =
    PaymentIntentionResult(
        orderId      = orderId,
        clientSecret = clientSecret,
        publicKey    = publicKey ?: com.dukkan.payment.BuildConfig.PAYMOB_PUBLIC_KEY,
    )
