package com.dukkan.payment.domain.usecase

import com.dukkan.domain.model.Address
import com.dukkan.domain.model.Money
import com.dukkan.domain.model.OrderConfirmation
import com.dukkan.payment.domain.repository.PaymentRepository
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
import java.math.BigDecimal

class ConfirmCashOrderUseCaseTest {
    private val repository = mockk<PaymentRepository>()
    private lateinit var useCase: ConfirmCashOrderUseCase

    @Before
    fun setUp() {
        useCase = ConfirmCashOrderUseCase(repository)
    }

    @Test
    fun `invoke calls repository confirmCashOrder`() = runTest {
        val address = Address(firstName = "John")
        val total = Money(BigDecimal.ZERO, "EGP")
        val result = Result.success(mockk<OrderConfirmation>())
        coEvery { repository.confirmCashOrder("key", address, "cart", total) } returns result

        val actual = useCase("key", address, "cart", total)

        assertEquals(result, actual)
        coVerify(exactly = 1) { repository.confirmCashOrder("key", address, "cart", total) }
    }
}
