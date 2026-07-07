package com.dukkan.data.source.remote.apollo

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.ApolloCall
import com.dukkan.GetCollectionsQuery
import com.dukkan.ProductsQuery
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsDataSourceImplTest {

    @MockK
    lateinit var apolloClient: ApolloClient

    private lateinit var dataSource: ProductsDataSourceImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        dataSource = ProductsDataSourceImpl(apolloClient)
        
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(android.util.Log::class)
    }

    @Test
    fun `fetchCategories filters collections by CATEGORY_HANDLES`() = runTest {
        // Arrange
        val nodes = listOf("men", "vans", "women").map { handle ->
            GetCollectionsQuery.Node(id = handle, title = handle, handle = handle)
        }

        val edges = nodes.map { node ->
            GetCollectionsQuery.Edge(node = node)
        }
        
        val mockData = GetCollectionsQuery.Data(
            collections = GetCollectionsQuery.Collections(edges = edges)
        )
        
        val mockResponse = ApolloResponse.Builder(
            operation = mockk<GetCollectionsQuery>(),
            requestUuid = mockk()
        ).data(mockData).build()
        
        val mockCall = mockk<ApolloCall<GetCollectionsQuery.Data>>()
        every { apolloClient.query(any<GetCollectionsQuery>()) } returns mockCall
        coEvery { mockCall.execute() } returns mockResponse

        // Act
        val categories = dataSource.fetchCategories()

        // Assert
        assertEquals(2, categories.size)
        assertEquals("men", categories[0].handle)
        assertEquals("women", categories[1].handle)
    }

    @Test
    fun `fetchBrands filters collections by BRAND_HANDLES`() = runTest {
        // Arrange
        val nodes = listOf("nike", "top", "adidas").map { handle ->
            GetCollectionsQuery.Node(id = handle, title = handle, handle = handle)
        }

        val edges = nodes.map { node ->
            GetCollectionsQuery.Edge(node = node)
        }
        
        val mockData = GetCollectionsQuery.Data(
            collections = GetCollectionsQuery.Collections(edges = edges)
        )
        
        val mockResponse = ApolloResponse.Builder(
            operation = mockk<GetCollectionsQuery>(),
            requestUuid = mockk()
        ).data(mockData).build()
        
        val mockCall = mockk<ApolloCall<GetCollectionsQuery.Data>>()
        every { apolloClient.query(any<GetCollectionsQuery>()) } returns mockCall
        coEvery { mockCall.execute() } returns mockResponse

        // Act
        val brands = dataSource.fetchBrands()

        // Assert
        assertEquals(2, brands.size)
        assertEquals("nike", brands[0].handle)
        assertEquals("adidas", brands[1].handle)
    }

    @Test(expected = Exception::class)
    fun `getProducts throws exception when response has errors`() = runTest {
        // Arrange
        val mockResponse = ApolloResponse.Builder(
            operation = mockk<ProductsQuery>(),
            requestUuid = mockk()
        ).errors(listOf(mockk(relaxed = true) { every { message } returns "GraphQL error" })).build()
        
        val mockCall = mockk<ApolloCall<ProductsQuery.Data>>()
        every { apolloClient.query(any<ProductsQuery>()) } returns mockCall
        coEvery { mockCall.execute() } returns mockResponse

        // Act
        dataSource.getProducts(10, null, "US", "en")
    }
}
