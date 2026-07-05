package com.dukkan.payment.domain.usecase

import com.dukkan.domain.model.Money
import com.dukkan.payment.domain.model.CreatedOrder
import com.dukkan.payment.domain.model.OrderCancelReason
import com.dukkan.payment.domain.model.OrderDraft
import com.dukkan.payment.domain.model.OrderFinancialStatus
import com.dukkan.payment.domain.model.OrderLineItemDraft
import com.dukkan.payment.domain.model.ShippingLineDraft
import com.dukkan.payment.domain.repository.OrderRepository
import java.math.BigDecimal
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CreateOrderUseCaseTest {

    @Test
    fun passesFinancialStatusToRepository() = runTest {
        val repository = FakeOrderRepository()
        val useCase = CreateOrderUseCase(repository)
        val draft = OrderDraft(
            customerId = "gid://shopify/Customer/1",
            lineItems = listOf(OrderLineItemDraft("gid://shopify/ProductVariant/1", 2)),
            shippingLine = ShippingLineDraft("Standard Shipping", "STANDARD", Money(BigDecimal.ZERO, "EGP")),
            currency = "EGP",
            shippingAddress = null,
        )

        useCase(draft, OrderFinancialStatus.PENDING)

        assertEquals(OrderFinancialStatus.PENDING, repository.status)
    }

    private class FakeOrderRepository : OrderRepository {
        var status: OrderFinancialStatus? = null

        override suspend fun createOrder(
            draft: OrderDraft,
            status: OrderFinancialStatus,
        ): Result<CreatedOrder> {
            this.status = status
            return Result.success(CreatedOrder("gid://shopify/Order/1", status.name, Money(BigDecimal.ZERO, "EGP")))
        }

        override suspend fun markOrderAsPaid(orderId: String): Result<CreatedOrder> =
            Result.success(CreatedOrder(orderId, "PAID", Money(BigDecimal.ZERO, "EGP")))

        override suspend fun cancelOrder(orderId: String, reason: OrderCancelReason): Result<Unit> =
            Result.success(Unit)
    }
}
