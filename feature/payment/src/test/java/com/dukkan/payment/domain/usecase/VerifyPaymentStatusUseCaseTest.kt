package com.dukkan.payment.domain.usecase

import com.dukkan.domain.model.Money
import com.dukkan.domain.model.OrderConfirmation
import com.dukkan.payment.domain.repository.PaymentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class VerifyPaymentStatusUseCaseTest {
    private val repository = mockk<PaymentRepository>()
    private lateinit var useCase: VerifyPaymentStatusUseCase

    @Before
    fun setUp() {
        useCase = VerifyPaymentStatusUseCase(repository)
    }

    @Test
    fun `invoke calls repository verifyPaymentStatus`() = runTest {
        val total = Money(BigDecimal.ZERO, "EGP")
        val result = Result.success(mockk<OrderConfirmation>())
        coEvery { repository.verifyPaymentStatus("ord_1", total) } returns result

        val actual = useCase("ord_1", total)

        assertEquals(result, actual)
        coVerify(exactly = 1) { repository.verifyPaymentStatus("ord_1", total) }
    }
}
