package com.dukkan.domain.usecase.cart

import com.dukkan.domain.repository.CartRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateCartQuantityUseCaseTest {
    private lateinit var useCase: UpdateCartQuantityUseCase
    private val repository: CartRepository = mockk()

    @Before
    fun setUp() {
        useCase = UpdateCartQuantityUseCase(repository)
    }

    @Test
    fun `invoke calls repository updateCartItemQuantity`() = runTest {
        val lineId = "line_1"
        val quantity = 5
        coEvery { repository.updateCartItemQuantity(lineId, quantity) } returns Unit

        useCase(lineId, quantity)

        coVerify(exactly = 1) { repository.updateCartItemQuantity(lineId, quantity) }
    }
}
