package com.dukkan.payment.domain.usecase

import com.dukkan.domain.model.Money
import com.dukkan.payment.domain.model.CheckoutAddress
import com.dukkan.payment.domain.model.PaymentIntentionResult
import com.dukkan.payment.domain.repository.PaymentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class CreatePaymentIntentionUseCaseTest {
    private val repository = mockk<PaymentRepository>()
    private lateinit var useCase: CreatePaymentIntentionUseCase

    @Before
    fun setUp() {
        useCase = CreatePaymentIntentionUseCase(repository)
    }

    @Test
    fun `invoke calls repository createPaymentIntention`() = runTest {
        val address = mockk<CheckoutAddress>()
        val total = Money(BigDecimal.ZERO, "EGP")
        val result = Result.success(mockk<PaymentIntentionResult>())
        coEvery { repository.createPaymentIntention("key", address, "cart", total) } returns result

        val actual = useCase("key", address, "cart", total)

        assertEquals(result, actual)
        coVerify(exactly = 1) { repository.createPaymentIntention("key", address, "cart", total) }
    }
}
