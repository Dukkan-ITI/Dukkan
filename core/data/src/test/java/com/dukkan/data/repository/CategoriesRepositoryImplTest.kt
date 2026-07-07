package com.dukkan.data.repository

import com.dukkan.data.source.remote.apollo.ProductsDataSource
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CategoriesRepositoryImplTest {

    private val productsDataSource: ProductsDataSource = mockk()
    private lateinit var repository: CategoriesRepositoryImpl

    @Before
    fun setUp() {
        mockkStatic(android.util.Log::class)
        every { android.util.Log.e(any(), any(), any()) } returns 0
        repository = CategoriesRepositoryImpl(productsDataSource)
    }

    @After
    fun tearDown() {
        unmockkStatic(android.util.Log::class)
    }

    @Test
    fun `getCategories returns mapped categories on success`() = runTest {
        // Arrange
        val remoteCategories = listOf(mockk<com.dukkan.GetCollectionsQuery.Node>(relaxed = true) {
            every { id } returns "cat-1"
            every { title } returns "Category 1"
            every { handle } returns "category-1"
        })
        coEvery { productsDataSource.fetchCategories() } returns remoteCategories

        // Act
        val result = repository.getCategories().first()

        // Assert
        assertEquals(1, result.size)
        assertEquals("cat-1", result[0].id)
        assertEquals("Category 1", result[0].name)
    }

    @Test
    fun `getCategories returns empty list on error`() = runTest {
        // Arrange
        coEvery { productsDataSource.fetchCategories() } throws Exception("Network error")

        // Act
        val result = repository.getCategories().first()

        // Assert
        assertTrue(result.isEmpty())
    }
}
