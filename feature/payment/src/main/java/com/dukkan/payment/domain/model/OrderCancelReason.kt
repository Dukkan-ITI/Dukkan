package com.dukkan.payment.domain.model

internal enum class OrderCancelReason {
    CUSTOMER,
    DECLINED,
    FRAUD,
    INVENTORY,
    OTHER,
    STAFF,
}
