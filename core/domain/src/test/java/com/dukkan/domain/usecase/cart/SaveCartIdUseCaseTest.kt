package com.dukkan.domain.usecase.cart

import com.dukkan.domain.repository.CartRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SaveCartIdUseCaseTest {
    private lateinit var useCase: SaveCartIdUseCase
    private val repository: CartRepository = mockk()

    @Before
    fun setUp() {
        useCase = SaveCartIdUseCase(repository)
    }

    @Test
    fun `invoke calls repository saveCartId`() = runTest {
        val cartId = "cart_123"
        coEvery { repository.saveCartId(cartId) } returns Unit

        useCase(cartId)

        coVerify(exactly = 1) { repository.saveCartId(cartId) }
    }
}
