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

class RegisterUseCaseTest {

    private lateinit var useCase: RegisterUseCase
    private val repository: AuthRepository = mockk()

    @Before
    fun setUp() {
        useCase = RegisterUseCase(repository)
    }

    @Test
    fun `invoke calls repository register`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        val firstName = "First"
        val lastName = "Last"
        val user = AuthUser("uid", email, "First Last")
        coEvery { repository.register(email, password, firstName, lastName) } returns Result.success(user)

        // When
        val result = useCase(email, password, firstName, lastName)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
        coVerify { repository.register(email, password, firstName, lastName) }
    }
}
