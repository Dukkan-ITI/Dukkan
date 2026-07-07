package com.dukkan.domain.usecase

import com.dukkan.domain.repository.AuthRepository
import com.dukkan.domain.repository.OrderRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SignOutUseCaseTest {

    private lateinit var useCase: SignOutUseCase
    private val authRepository: AuthRepository = mockk()
    private val orderRepository: OrderRepository = mockk()

    @Before
    fun setUp() {
        useCase = SignOutUseCase(authRepository, orderRepository)
    }

    @Test
    fun `invoke calls repository signOut and clearLocalOrders`() = runTest {
        // Given
        coEvery { authRepository.signOut() } returns Unit
        coEvery { orderRepository.clearLocalOrders() } returns Unit

        // When
        useCase()

        // Then
        coVerify { authRepository.signOut() }
        coVerify { orderRepository.clearLocalOrders() }
    }
}
