package com.dukkan.domain.usecase.cart

import com.dukkan.domain.repository.CartRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SyncCartOnLoginUseCaseTest {
    private lateinit var useCase: SyncCartOnLoginUseCase
    private val repository: CartRepository = mockk()

    @Before
    fun setUp() {
        useCase = SyncCartOnLoginUseCase(repository)
    }

    @Test
    fun `invoke calls repository syncCartOnLogin`() = runTest {
        val userId = "user_1"
        coEvery { repository.syncCartOnLogin(userId) } returns Unit

        useCase(userId)

        coVerify(exactly = 1) { repository.syncCartOnLogin(userId) }
    }
}
