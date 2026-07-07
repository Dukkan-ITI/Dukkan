package com.dukkan.domain.usecase.cart

import com.dukkan.domain.repository.CartRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AddToCartUseCaseTest {
    private lateinit var useCase: AddToCartUseCase
    private val repository: CartRepository = mockk()

    @Before
    fun setUp() {
        useCase = AddToCartUseCase(repository)
    }

    @Test
    fun `invoke calls repository addCartItem`() = runTest {
        val variantId = "var_1"
        coEvery { repository.addCartItem(variantId) } returns Unit

        useCase(variantId)

        coVerify(exactly = 1) { repository.addCartItem(variantId) }
    }
}
