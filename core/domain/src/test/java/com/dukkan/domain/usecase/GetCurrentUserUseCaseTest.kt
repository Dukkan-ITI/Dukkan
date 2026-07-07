package com.dukkan.domain.usecase

import com.dukkan.domain.model.AuthUser
import com.dukkan.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetCurrentUserUseCaseTest {

    private lateinit var useCase: GetCurrentUserUseCase
    private val repository: AuthRepository = mockk()

    @Before
    fun setUp() {
        useCase = GetCurrentUserUseCase(repository)
    }

    @Test
    fun `invoke returns repository current user`() = runTest {
        // Given
        val user = AuthUser("uid", "email", "Name")
        coEvery { repository.getCurrentUser() } returns user

        // When
        val result = useCase()

        // Then
        assertEquals(user, result)
    }
}
