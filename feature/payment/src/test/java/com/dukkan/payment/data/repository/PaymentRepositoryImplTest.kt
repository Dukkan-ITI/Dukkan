package com.dukkan.payment.data.repository

import com.dukkan.domain.model.Address
import com.dukkan.domain.model.Money
import com.dukkan.payment.data.remote.PaymentApi
import com.dukkan.payment.data.remote.dto.PaymobIntentionResponse
import com.dukkan.payment.domain.model.CheckoutAddress
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

internal class PaymentRepositoryImplTest {

    @MockK
    lateinit var api: PaymentApi

    private lateinit var repository: PaymentRepositoryImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = PaymentRepositoryImpl(api)
    }

    @Test
    fun `confirmCashOrder returns Success confirmation`() = runTest {
        val cartTotal = Money(BigDecimal("100.0"), "EGP")
        val result = repository.confirmCashOrder(
            idempotencyKey = "key",
            address = mockk<CheckoutAddress.Saved>(),
            cartId = "cart_1",
            cartTotal = cartTotal
        )

        assertTrue(result.isSuccess)
        val confirmation = result.getOrNull()
        assertNotNull(confirmation)
        assertTrue(confirmation?.orderId?.startsWith("CASH-") == true)
        assertEquals(cartTotal, confirmation?.total)
    }

    @Test
    fun `createPaymentIntention success returns result`() = runTest {
        val address = Address(firstName = "John", lastName = "Doe")
        val checkoutAddress = CheckoutAddress.OneOff(address)
        val cartTotal = Money(BigDecimal("100.0"), "EGP")
        
        val mockResponse = PaymobIntentionResponse(
            clientSecret = "secret",
            publicKey = "pub"
        )

        coEvery { api.createIntention(any(), any()) } returns mockResponse

        val result = repository.createPaymentIntention(
            idempotencyKey = "key",
            address = checkoutAddress,
            cartId = "cart_1",
            cartTotal = cartTotal
        )

        assertTrue(result.isSuccess)
        assertEquals("secret", result.getOrNull()?.clientSecret)
        assertEquals("pub", result.getOrNull()?.publicKey)
    }

    @Test
    fun `verifyPaymentStatus returns Success confirmation`() = runTest {
        val cartTotal = Money(BigDecimal("100.0"), "EGP")
        val result = repository.verifyPaymentStatus("ord_1", cartTotal)

        assertTrue(result.isSuccess)
        assertEquals("ord_1", result.getOrNull()?.orderId)
        assertEquals(cartTotal, result.getOrNull()?.total)
    }
}
