package com.dukkan.payment.domain.repository

import com.dukkan.payment.domain.model.CreatedOrder
import com.dukkan.payment.domain.model.OrderCancelReason
import com.dukkan.payment.domain.model.OrderDraft
import com.dukkan.payment.domain.model.OrderFinancialStatus

internal interface OrderRepository {
    suspend fun createOrder(draft: OrderDraft, status: OrderFinancialStatus): Result<CreatedOrder>
    suspend fun markOrderAsPaid(orderId: String): Result<CreatedOrder>
    suspend fun cancelOrder(orderId: String, reason: OrderCancelReason): Result<Unit>
}
