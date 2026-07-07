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

class LoginWithGoogleUseCaseTest {

    private lateinit var useCase: LoginWithGoogleUseCase
    private val repository: AuthRepository = mockk()

    @Before
    fun setUp() {
        useCase = LoginWithGoogleUseCase(repository)
    }

    @Test
    fun `invoke calls repository loginWithGoogle`() = runTest {
        // Given
        val idToken = "token"
        val user = AuthUser("uid", "email", "Name")
        coEvery { repository.loginWithGoogle(idToken) } returns Result.success(user)

        // When
        val result = useCase(idToken)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
        coVerify { repository.loginWithGoogle(idToken) }
    }
}
