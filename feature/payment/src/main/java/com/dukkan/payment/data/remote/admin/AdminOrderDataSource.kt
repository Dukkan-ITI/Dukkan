package com.dukkan.payment.data.remote.admin

import com.dukkan.payment.admin.OrderCancelMutation
import com.dukkan.payment.admin.OrderCreateMutation
import com.dukkan.payment.admin.OrderMarkAsPaidMutation
import com.dukkan.payment.admin.type.OrderCancelReason
import com.dukkan.payment.admin.type.OrderCreateOrderInput
import com.dukkan.payment.admin.type.OrderCreateOptionsInput
import com.dukkan.payment.admin.type.OrderMarkAsPaidInput

internal interface AdminOrderDataSource {
    suspend fun createOrder(
        order: OrderCreateOrderInput,
        options: OrderCreateOptionsInput?,
    ): OrderCreateMutation.OrderCreate

    suspend fun markOrderAsPaid(input: OrderMarkAsPaidInput): OrderMarkAsPaidMutation.OrderMarkAsPaid

    suspend fun cancelOrder(
        orderId: String,
        reason: OrderCancelReason,
        restock: Boolean,
    ): OrderCancelMutation.OrderCancel
}
