package com.dukkan.domain.usecase.cart

import com.dukkan.domain.repository.CartRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ClearCartFullyUseCaseTest {
    private lateinit var useCase: ClearCartFullyUseCase
    private val repository: CartRepository = mockk()

    @Before
    fun setUp() {
        useCase = ClearCartFullyUseCase(repository)
    }

    @Test
    fun `invoke calls repository clearCartFully`() = runTest {
        coEvery { repository.clearCartFully() } returns Unit

        useCase()

        coVerify(exactly = 1) { repository.clearCartFully() }
    }
}
