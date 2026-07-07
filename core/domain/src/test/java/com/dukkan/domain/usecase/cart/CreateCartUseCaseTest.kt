package com.dukkan.domain.usecase.cart

import com.dukkan.domain.repository.CartRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CreateCartUseCaseTest {
    private lateinit var useCase: CreateCartUseCase
    private val repository: CartRepository = mockk()

    @Before
    fun setUp() {
        useCase = CreateCartUseCase(repository)
    }

    @Test
    fun `invoke calls repository createCart`() = runTest {
        val token = "token"
        val expectedId = "cart_id"
        coEvery { repository.createCart(token) } returns expectedId

        val result = useCase(token)

        assertEquals(expectedId, result)
    }
}
