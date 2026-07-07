package com.dukkan.domain.usecase

import com.dukkan.domain.model.AuthUser
import com.dukkan.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginUseCaseTest {

    private lateinit var useCase: LoginUseCase
    private val repository: AuthRepository = mockk()

    @Before
    fun setUp() {
        useCase = LoginUseCase(repository)
    }

    @Test
    fun `invoke calls repository login`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        val user = AuthUser("uid", email, "Name")
        coEvery { repository.login(email, password) } returns Result.success(user)

        // When
        val result = useCase(email, password)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
        coVerify { repository.login(email, password) }
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        val exception = Exception("Failed")
        coEvery { repository.login(email, password) } returns Result.failure(exception)

        // When
        val result = useCase(email, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
