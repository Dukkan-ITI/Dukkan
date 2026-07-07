package com.dukkan.domain.usecase.cart

import com.dukkan.domain.model.cart.CartLine
import io.mockk.every
import io.mockk.mockk
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalculateCartTotalsUseCaseTest {
    private lateinit var useCase: CalculateCartTotalsUseCase

    @Before
    fun setUp() {
        useCase = CalculateCartTotalsUseCase()
    }

    @Test
    fun `invoke with empty lines returns zero totals`() {
        val result = useCase(emptyList())

        assertEquals(0.0, result.subtotal, 0.001)
        assertEquals(0.0, result.shipping, 0.001)
        assertEquals(0.0, result.total, 0.001)
    }

    @Test
    fun `invoke with lines calculates correct subtotal and total`() {
        val line1 = mockk<CartLine> {
            every { cost.totalAmount.amount } returns BigDecimal("10.0")
        }
        val line2 = mockk<CartLine> {
            every { cost.totalAmount.amount } returns BigDecimal("25.5")
        }
        
        val shipping = 5.0
        val result = useCase(listOf(line1, line2), shipping)

        assertEquals(35.5, result.subtotal, 0.001)
        assertEquals(5.0, result.shipping, 0.001)
        assertEquals(40.5, result.total, 0.001)
    }

    @Test
    fun `invoke uses default shipping cost`() {
        val line1 = mockk<CartLine> {
            every { cost.totalAmount.amount } returns BigDecimal("10.0")
        }
        
        val result = useCase(listOf(line1))

        assertEquals(6.0, result.shipping, 0.001)
        assertEquals(16.0, result.total, 0.001)
    }
}
