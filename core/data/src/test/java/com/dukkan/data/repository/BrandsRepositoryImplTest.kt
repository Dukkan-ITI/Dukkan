package com.dukkan.data.repository

import com.dukkan.data.source.remote.apollo.ProductsDataSource
import com.dukkan.domain.model.Brand
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

class BrandsRepositoryImplTest {

    private val productsDataSource: ProductsDataSource = mockk()
    private lateinit var repository: BrandsRepositoryImpl

    @Before
    fun setUp() {
        mockkStatic(android.util.Log::class)
        every { android.util.Log.e(any(), any(), any()) } returns 0
        repository = BrandsRepositoryImpl(productsDataSource)
    }

    @After
    fun tearDown() {
        unmockkStatic(android.util.Log::class)
    }

    @Test
    fun `getBrands returns mapped brands on success`() = runTest {
        // Arrange
        val remoteBrands = listOf(mockk<com.dukkan.GetCollectionsQuery.Node>(relaxed = true) {
            every { id } returns "1"
            every { title } returns "Brand 1"
        })
        coEvery { productsDataSource.fetchBrands() } returns remoteBrands

        // Act
        val result = repository.getBrands().first()

        // Assert
        assertEquals(1, result.size)
        assertEquals("1", result[0].id)
        assertEquals("Brand 1", result[0].name)
    }

    @Test
    fun `getBrands returns empty list on error`() = runTest {
        // Arrange
        coEvery { productsDataSource.fetchBrands() } throws Exception("Network error")

        // Act
        val result = repository.getBrands().first()

        // Assert
        assertTrue(result.isEmpty())
    }
}
