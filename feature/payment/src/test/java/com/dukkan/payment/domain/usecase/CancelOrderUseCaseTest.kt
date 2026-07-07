package com.dukkan.payment.domain.usecase

import com.dukkan.payment.domain.model.OrderCancelReason
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

class CancelOrderUseCaseTest {
    private val repository = mockk<OrderRepository>()
    private lateinit var useCase: CancelOrderUseCase

    @Before
    fun setUp() {
        useCase = CancelOrderUseCase(repository)
    }

    @Test
    fun `invoke calls repository cancelOrder`() = runTest {
        val result = Result.success(Unit)
        coEvery { repository.cancelOrder("ord_1", OrderCancelReason.CUSTOMER) } returns result

        val actual = useCase("ord_1", OrderCancelReason.CUSTOMER)

        assertEquals(result, actual)
        coVerify(exactly = 1) { repository.cancelOrder("ord_1", OrderCancelReason.CUSTOMER) }
    }
}
