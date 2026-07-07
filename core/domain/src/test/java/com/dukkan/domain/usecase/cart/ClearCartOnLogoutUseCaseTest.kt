package com.dukkan.domain.usecase.cart

import com.dukkan.domain.repository.CartRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ClearCartOnLogoutUseCaseTest {
    private lateinit var useCase: ClearCartOnLogoutUseCase
    private val repository: CartRepository = mockk()

    @Before
    fun setUp() {
        useCase = ClearCartOnLogoutUseCase(repository)
    }

    @Test
    fun `invoke calls repository clearLocalCart`() = runTest {
        coEvery { repository.clearLocalCart() } returns Unit

        useCase()

        coVerify(exactly = 1) { repository.clearLocalCart() }
    }
}
