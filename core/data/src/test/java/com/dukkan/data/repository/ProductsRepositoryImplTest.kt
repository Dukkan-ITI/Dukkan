package com.dukkan.data.repository

import com.dukkan.ProductQuery
import com.dukkan.ProductsQuery
import com.dukkan.data.source.remote.apollo.ProductsDataSource
import com.dukkan.domain.model.AppCurrency
import com.dukkan.domain.model.AppLanguage
import com.dukkan.domain.model.Money
import com.dukkan.domain.model.NetworkImage
import com.dukkan.domain.model.Product
import com.dukkan.domain.repository.SettingsRepository
import com.dukkan.type.CountryCode
import com.dukkan.type.CurrencyCode
import com.dukkan.type.LanguageCode
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsRepositoryImplTest {

    @MockK
    lateinit var productsDataSource: ProductsDataSource

    @MockK
    lateinit var settingsRepository: SettingsRepository

    private lateinit var repository: ProductsRepositoryImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { settingsRepository.currency } returns flowOf(AppCurrency.EGP)
        every { settingsRepository.language } returns flowOf(AppLanguage.ARABIC)
        repository = ProductsRepositoryImpl(productsDataSource, settingsRepository)
    }

    @Test
    fun `getProducts returns mapped list and forwards settings`() = runTest {
        val expected = listOf(
            Product(
                id = "prod-1",
                title = "Product 1",
                featuredImage = NetworkImage(
                    url = "https://cdn.example.com/p1.jpg",
                    blurredUrl = "blur-1",
                    altText = null,
                ),
                minPrice = Money.from("10.00", "USD"),
                maxPrice = Money.from("12.00", "USD"),
                description = "Description 1",
                productType = null,
                images = null,
                variants = null,
            )
        )
        val response = ProductsQuery.Data(
            products = ProductsQuery.Products(
                pageInfo = ProductsQuery.PageInfo(hasNextPage = false, endCursor = null),
                edges = listOf(
                    ProductsQuery.Edge(
                        node = ProductsQuery.Node(
                            id = "prod-1",
                            handle = "product-1",
                            title = "Product 1",
                            productType = "Shirt",
                            description = "Description 1",
                            featuredImage = ProductsQuery.FeaturedImage(
                                url = "https://cdn.example.com/p1.jpg",
                                thumbhash = "blur-1",
                            ),
                            priceRange = ProductsQuery.PriceRange(
                                maxVariantPrice = ProductsQuery.MaxVariantPrice(
                                    amount = "12.00",
                                    currencyCode = CurrencyCode.USD,
                                ),
                                minVariantPrice = ProductsQuery.MinVariantPrice(
                                    amount = "10.00",
                                    currencyCode = CurrencyCode.USD,
                                ),
                            ),
                        )
                    )
                )
            )
        )

        coEvery {
            productsDataSource.getProducts(first = 10, after = null, country = "EG", language = "AR")
        } returns response

        val result = repository.getProducts(limit = 10, after = null)

        assertEquals(expected, result)
        coVerify {
            productsDataSource.getProducts(first = 10, after = null, country = "EG", language = "AR")
        }
    }

    @Test
    fun `getProducts returns empty list when response is null`() = runTest {
        coEvery {
            productsDataSource.getProducts(first = 10, after = null, country = "EG", language = "AR")
        } returns null

        val result = repository.getProducts(limit = 10, after = null)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getProductById returns mapped product on success`() = runTest {
        val productId = "prod_123"
        val response = ProductQuery.Product(
            id = productId,
            title = "Product Title",
            handle = "product-title",
            featuredImage = ProductQuery.FeaturedImage(
                altText = "Alt",
                url = "https://cdn.example.com/detail.jpg",
                thumbhash = "detail-blur",
            ),
            productType = "Shoes",
            priceRange = ProductQuery.PriceRange(
                maxVariantPrice = ProductQuery.MaxVariantPrice(
                    amount = "25.00",
                    currencyCode = CurrencyCode.USD,
                ),
                minVariantPrice = ProductQuery.MinVariantPrice(
                    amount = "20.00",
                    currencyCode = CurrencyCode.USD,
                ),
            ),
            description = "Detail description",
            images = ProductQuery.Images(
                nodes = listOf(
                    ProductQuery.Node(
                        altText = "Gallery",
                        url = "https://cdn.example.com/gallery.jpg",
                        thumbhash = "gallery-blur",
                    )
                )
            ),
            variants = ProductQuery.Variants(
                nodes = listOf(
                    ProductQuery.Node1(
                        availableForSale = true,
                        currentlyNotInStock = false,
                        id = "variant-1",
                        quantityAvailable = 7,
                        title = "Variant 1",
                        image = ProductQuery.Image(
                            altText = "Variant image",
                            url = "https://cdn.example.com/variant.jpg",
                            thumbhash = "variant-blur",
                        ),
                        price = ProductQuery.Price(
                            amount = "9.99",
                            currencyCode = CurrencyCode.USD,
                        ),
                    )
                )
            ),
        )

        coEvery {
            productsDataSource.getProductById(productId, country = "EG", language = "AR")
        } returns response

        val result = repository.getProductById(productId)

        assertTrue(result.isSuccess)
        val product = result.getOrNull()!!
        assertEquals(productId, product.id)
        assertEquals("Product Title", product.title)
        assertEquals(
            NetworkImage(
                url = "https://cdn.example.com/detail.jpg",
                blurredUrl = "detail-blur",
                altText = "Alt",
            ),
            product.featuredImage
        )
        assertEquals(Money.from("20.00", "USD"), product.minPrice)
        assertEquals(Money.from("25.00", "USD"), product.maxPrice)
        assertEquals(listOf(NetworkImage("https://cdn.example.com/gallery.jpg", "gallery-blur", "Gallery")), product.images)
        coVerify {
            productsDataSource.getProductById(productId, country = "EG", language = "AR")
        }
    }

    @Test
    fun `getProductById returns failure on null response`() = runTest {
        val productId = "invalid_id"
        coEvery {
            productsDataSource.getProductById(productId, country = "EG", language = "AR")
        } returns null

        val result = repository.getProductById(productId)

        assertTrue(result.isFailure)
        assertEquals("Unknown error occurred", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getProductsByCollectionId forwards settings and returns data source result`() = runTest {
        val expected = listOf(
            mockk<Product>(relaxed = true),
            mockk<Product>(relaxed = true),
        )
        coEvery {
            productsDataSource.getProductsByCollectionId(
                categoryId = "summer",
                first = 5,
                after = "cursor-1",
                country = "EG",
                language = "AR",
            )
        } returns expected

        val result = repository.getProductsByCollectionId(
            categoryId = "summer",
            limit = 5,
            after = "cursor-1",
        )

        assertEquals(expected, result)
        coVerify {
            productsDataSource.getProductsByCollectionId(
                categoryId = "summer",
                first = 5,
                after = "cursor-1",
                country = "EG",
                language = "AR",
            )
        }
    }

    @Test
    fun `getProductsByType forwards settings and returns data source result`() = runTest {
        val expected = listOf(
            mockk<Product>(relaxed = true)
        )
        coEvery {
            productsDataSource.getProductsByVendor(
                vendor = "nike",
                first = 7,
                after = null,
                country = "EG",
                language = "AR",
            )
        } returns expected

        val result = repository.getProductsByType(
            type = "nike",
            limit = 7,
            after = null,
        )

        assertEquals(expected, result)
        coVerify {
            productsDataSource.getProductsByVendor(
                vendor = "nike",
                first = 7,
                after = null,
                country = "EG",
                language = "AR",
            )
        }
    }
}
