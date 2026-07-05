package com.dukkan.payment.data.repository

import com.dukkan.payment.data.mapper.defaultOrderCreateOptions
import com.dukkan.payment.data.mapper.toAdminCancelReason
import com.dukkan.payment.data.mapper.toAdminInput
import com.dukkan.payment.data.mapper.toDomain
import com.dukkan.payment.data.mapper.toMarkAsPaidInput
import com.dukkan.payment.data.remote.admin.AdminOrderDataSource
import com.dukkan.payment.domain.model.CreatedOrder
import com.dukkan.payment.domain.model.OrderCancelReason
import com.dukkan.payment.domain.model.OrderDraft
import com.dukkan.payment.domain.model.OrderFinancialStatus
import com.dukkan.payment.domain.repository.OrderRepository
import javax.inject.Inject

internal class OrderRepositoryImpl @Inject constructor(
    private val dataSource: AdminOrderDataSource,
) : OrderRepository {

    override suspend fun createOrder(
        draft: OrderDraft,
        status: OrderFinancialStatus,
    ): Result<CreatedOrder> = runCatching {
        val result = dataSource.createOrder(
            order = draft.toAdminInput(status),
            options = defaultOrderCreateOptions(),
        )
        val userErrors = result.userErrors
        if (userErrors.isNotEmpty()) {
            throw Exception(userErrors.joinToString("; ") { it.message })
        }
        result.order?.toDomain() ?: throw Exception("Shopify orderCreate returned no order")
    }

    override suspend fun markOrderAsPaid(orderId: String): Result<CreatedOrder> = runCatching {
        val result = dataSource.markOrderAsPaid(orderId.toMarkAsPaidInput())
        val userErrors = result.userErrors
        if (userErrors.isNotEmpty()) {
            throw Exception(userErrors.joinToString("; ") { it.message })
        }
        result.order?.toDomain() ?: throw Exception("Shopify orderMarkAsPaid returned no order")
    }

    override suspend fun cancelOrder(orderId: String, reason: OrderCancelReason): Result<Unit> = runCatching {
        val result = dataSource.cancelOrder(
            orderId = orderId,
            reason = reason.toAdminCancelReason(),
            restock = true,
        )
        val userErrors = result.orderCancelUserErrors
        if (userErrors.isNotEmpty()) {
            throw Exception(userErrors.joinToString("; ") { it.message })
        }
    }

    override suspend fun deleteOrder(orderId: String): Result<Unit> = runCatching {
        val result = dataSource.deleteOrder(orderId)
        val userErrors = result.userErrors
        if (userErrors.isNotEmpty()) {
            throw Exception(userErrors.joinToString("; ") { it.message })
        }
    }
}
