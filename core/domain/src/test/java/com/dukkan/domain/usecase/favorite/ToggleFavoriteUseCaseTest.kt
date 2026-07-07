package com.dukkan.domain.usecase.favorite

import com.dukkan.domain.model.FavoriteProduct
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ToggleFavoriteUseCaseTest {

    private val add: AddFavoriteUseCase = mockk()
    private val remove: RemoveFavoriteUseCase = mockk()
    private lateinit var useCase: ToggleFavoriteUseCase

    @Before
    fun setUp() {
        useCase = ToggleFavoriteUseCase(add, remove)
    }

    @Test
    fun `when isFav is true, remove is called`() = runTest {
        // Arrange
        val product = FavoriteProduct("id", "title", "image", "10.0", "USD")
        coEvery { remove(any()) } returns Unit

        // Act
        useCase(product, true)

        // Assert
        coVerify(exactly = 1) { remove(product.id) }
        coVerify(exactly = 0) { add(any()) }
    }

    @Test
    fun `when isFav is false, add is called`() = runTest {
        // Arrange
        val product = FavoriteProduct("id", "title", "image", "10.0", "USD")
        coEvery { add(any()) } returns Unit

        // Act
        useCase(product, false)

        // Assert
        coVerify(exactly = 1) { add(product) }
        coVerify(exactly = 0) { remove(any()) }
    }
}
