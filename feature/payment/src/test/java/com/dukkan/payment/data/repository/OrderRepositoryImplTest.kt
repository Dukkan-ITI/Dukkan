package com.dukkan.payment.data.repository

import com.dukkan.payment.admin.OrderCancelMutation
import com.dukkan.payment.admin.OrderCreateMutation
import com.dukkan.payment.admin.OrderMarkAsPaidMutation
import com.dukkan.payment.admin.OrderDeleteMutation
import com.dukkan.payment.data.remote.admin.AdminOrderDataSource
import com.dukkan.payment.domain.model.*
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class OrderRepositoryImplTest {

    @MockK
    lateinit var dataSource: AdminOrderDataSource

    private lateinit var repository: OrderRepositoryImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = OrderRepositoryImpl(dataSource)
    }

    @Test
    fun `createOrder success returns CreatedOrder`() = runTest {
        val draft = mockk<OrderDraft>(relaxed = true)
        val mockOrder = mockk<OrderCreateMutation.Order>(relaxed = true) {
            every { id } returns "ord_1"
            every { displayFinancialStatus?.rawValue } returns "PAID"
            every { currentTotalPriceSet.shopMoney.amount } returns "100.0"
            every { currentTotalPriceSet.shopMoney.currencyCode.rawValue } returns "EGP"
        }
        val mockResponse = mockk<OrderCreateMutation.OrderCreate> {
            every { userErrors } returns emptyList()
            every { order } returns mockOrder
        }
        
        coEvery { dataSource.createOrder(any(), any()) } returns mockResponse

        val result = repository.createOrder(draft, OrderFinancialStatus.PAID)

        assertTrue(result.isSuccess)
        assertEquals("ord_1", result.getOrNull()?.orderId)
    }

    @Test
    fun `createOrder failure when user errors present`() = runTest {
        val mockResponse = mockk<OrderCreateMutation.OrderCreate> {
            every { userErrors } returns listOf(mockk { every { message } returns "Error 1" })
            every { order } returns null
        }
        coEvery { dataSource.createOrder(any(), any()) } returns mockResponse

        val result = repository.createOrder(mockk(relaxed = true), OrderFinancialStatus.PAID)

        assertTrue(result.isFailure)
        assertEquals("Error 1", result.exceptionOrNull()?.message)
    }

    @Test
    fun `markOrderAsPaid success`() = runTest {
        val mockOrder = mockk<OrderMarkAsPaidMutation.Order>(relaxed = true) {
            every { id } returns "ord_1"
            every { displayFinancialStatus?.rawValue } returns "PAID"
            every { currentTotalPriceSet?.shopMoney?.amount } returns "100.0"
            every { currentTotalPriceSet?.shopMoney?.currencyCode?.rawValue } returns "EGP"
        }
        coEvery { dataSource.markOrderAsPaid(any()) } returns mockk {
            every { userErrors } returns emptyList()
            every { order } returns mockOrder
        }

        val result = repository.markOrderAsPaid("ord_1")

        assertTrue(result.isSuccess)
        assertEquals("ord_1", result.getOrNull()?.orderId)
    }

    @Test
    fun `cancelOrder success`() = runTest {
        coEvery { dataSource.cancelOrder(any(), any(), any()) } returns mockk {
            every { orderCancelUserErrors } returns emptyList()
        }

        val result = repository.cancelOrder("ord_1", OrderCancelReason.CUSTOMER)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `deleteOrder success`() = runTest {
        coEvery { dataSource.deleteOrder(any()) } returns mockk {
            every { userErrors } returns emptyList()
        }

        val result = repository.deleteOrder("ord_1")

        assertTrue(result.isSuccess)
    }
}
