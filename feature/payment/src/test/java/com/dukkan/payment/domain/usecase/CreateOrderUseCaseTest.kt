package com.dukkan.payment.domain.usecase

import com.dukkan.payment.domain.model.CreatedOrder
import com.dukkan.payment.domain.model.OrderDraft
import com.dukkan.payment.domain.model.OrderFinancialStatus
import com.dukkan.payment.domain.repository.OrderRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CreateOrderUseCaseTest {
    private val repository = mockk<OrderRepository>()
    private lateinit var useCase: CreateOrderUseCase

    @Before
    fun setUp() {
        useCase = CreateOrderUseCase(repository)
    }

    @Test
    fun `invoke calls repository createOrder`() = runTest {
        val draft = mockk<OrderDraft>()
        val result = Result.success(mockk<CreatedOrder>())
        coEvery { repository.createOrder(draft, OrderFinancialStatus.PAID) } returns result

        val actual = useCase(draft, OrderFinancialStatus.PAID)

        assertEquals(result, actual)
        coVerify(exactly = 1) { repository.createOrder(draft, OrderFinancialStatus.PAID) }
    }
}
