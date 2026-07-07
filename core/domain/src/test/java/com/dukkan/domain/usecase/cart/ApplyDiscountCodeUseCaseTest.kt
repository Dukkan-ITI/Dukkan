package com.dukkan.domain.usecase.cart

import com.dukkan.domain.repository.CartRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ApplyDiscountCodeUseCaseTest {
    private lateinit var useCase: ApplyDiscountCodeUseCase
    private val repository: CartRepository = mockk()

    @Before
    fun setUp() {
        useCase = ApplyDiscountCodeUseCase(repository)
    }

    @Test
    fun `invoke calls repository applyDiscountCode and returns result`() = runTest {
        val code = "PROMO10"
        val expectedResult = Result.success(Unit)
        coEvery { repository.applyDiscountCode(code) } returns expectedResult

        val result = useCase(code)

        assertEquals(expectedResult, result)
    }
}
