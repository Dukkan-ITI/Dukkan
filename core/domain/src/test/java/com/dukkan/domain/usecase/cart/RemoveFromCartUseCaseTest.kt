package com.dukkan.domain.usecase.cart

import com.dukkan.domain.repository.CartRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RemoveFromCartUseCaseTest {
    private lateinit var useCase: RemoveFromCartUseCase
    private val repository: CartRepository = mockk()

    @Before
    fun setUp() {
        useCase = RemoveFromCartUseCase(repository)
    }

    @Test
    fun `invoke calls repository removeCartItem`() = runTest {
        val lineId = "line_1"
        coEvery { repository.removeCartItem(lineId) } returns Unit

        useCase(lineId)

        coVerify(exactly = 1) { repository.removeCartItem(lineId) }
    }
}
