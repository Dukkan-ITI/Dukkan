package com.dukkan.domain.usecase.cart

import com.dukkan.domain.model.cart.StoreCart
import com.dukkan.domain.repository.CartRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetCartItemsUseCaseTest {
    private lateinit var useCase: GetCartItemsUseCase
    private val repository: CartRepository = mockk()

    @Before
    fun setUp() {
        useCase = GetCartItemsUseCase(repository)
    }

    @Test
    fun `invoke calls repository getCart`() = runTest {
        val mockCart = mockk<StoreCart>()
        coEvery { repository.getCart() } returns mockCart

        val result = useCase()

        assertEquals(mockCart, result)
    }
}
