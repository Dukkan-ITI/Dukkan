package com.dukkan.home.viewmodel

import app.cash.turbine.test
import com.dukkan.domain.model.FavoriteProduct
import com.dukkan.domain.model.Money
import com.dukkan.domain.model.Product
import com.dukkan.domain.usecase.favorite.GetFavoritesUseCase
import com.dukkan.domain.usecase.favorite.ToggleFavoriteUseCase
import com.dukkan.domain.usecase.product.GetProductsUseCase
import com.dukkan.home.R
import com.dukkan.home.uistate.AllProductsUiState
import com.dukkan.home.uistate.UiText
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class AllProductsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    lateinit var getProductsUseCase: GetProductsUseCase

    @MockK
    lateinit var getFavoritesUseCase: GetFavoritesUseCase

    @MockK
    lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase

    private val favoritesFlow = MutableStateFlow<List<FavoriteProduct>>(emptyList())
    private val createdViewModels = mutableListOf<Any>()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        coEvery { getProductsUseCase(limit = 50) } returns emptyList()
        io.mockk.every { getFavoritesUseCase() } returns favoritesFlow
    }

    @After
    fun tearDown() {
        clearCreatedViewModels()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() {
        val viewModel = createViewModel()
        assertEquals(AllProductsUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `loadProducts success transitions to Success`() = runTest(testDispatcher) {
        val products = listOf(
            product(id = "p1", title = "Product 1"),
            product(id = "p2", title = "Product 2")
        )
        favoritesFlow.value = listOf(FavoriteProduct("p1", "Product 1", "", "10.0", "USD"))
        coEvery { getProductsUseCase(limit = 50) } returns products

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(AllProductsUiState.Loading, awaitItem())
            advanceUntilIdle()

            val state = awaitItem()
            assertTrue(state is AllProductsUiState.Success)
            state as AllProductsUiState.Success
            assertEquals(products, state.products)
            assertEquals(setOf("p1"), state.favoriteIds)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `favorite flow updates favorite ids in success state`() = runTest(testDispatcher) {
        val products = listOf(product(id = "p1", title = "Product 1"))
        coEvery { getProductsUseCase(limit = 50) } returns products
        favoritesFlow.value = listOf(FavoriteProduct("p1", "Product 1", "", "10.0", "USD"))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(AllProductsUiState.Loading, awaitItem())
            advanceUntilIdle()

            val firstSuccess = awaitItem() as AllProductsUiState.Success
            assertEquals(setOf("p1"), firstSuccess.favoriteIds)

            favoritesFlow.value = listOf(FavoriteProduct("p2", "Product 2", "", "20.0", "USD"))
            advanceUntilIdle()

            val secondSuccess = awaitItem() as AllProductsUiState.Success
            assertEquals(setOf("p2"), secondSuccess.favoriteIds)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadProducts failure transitions to Error`() = runTest(testDispatcher) {
        coEvery { getProductsUseCase(limit = 50) } throws Exception("Network Error")

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(AllProductsUiState.Loading, awaitItem())
            advanceUntilIdle()

            val state = awaitItem()
            assertTrue(state is AllProductsUiState.Error)
            val error = state as AllProductsUiState.Error
            assertTrue(error.message is UiText.StringResource)
            assertEquals(R.string.error_loading_products, (error.message as UiText.StringResource).resId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onFavoriteClick forwards mapped product to toggleFavoriteUseCase`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        val product = product(id = "p1", title = "Product 1")
        val slot = slot<FavoriteProduct>()

        coEvery { toggleFavoriteUseCase(capture(slot), false) } returns Unit

        viewModel.onFavoriteClick(product, false)
        advanceUntilIdle()

        coVerify(exactly = 1) { toggleFavoriteUseCase(any(), false) }
        assertEquals(
            FavoriteProduct(
                id = "p1",
                title = "Product 1",
                imageUrl = "",
                price = "10.0",
                currencyCode = "USD",
            ),
            slot.captured
        )
    }

    private fun createViewModel(): AllProductsViewModel {
        return AllProductsViewModel(
            getProductsUseCase,
            getFavoritesUseCase,
            toggleFavoriteUseCase,
        ).also { createdViewModels += it }
    }

    private fun clearCreatedViewModels() {
        createdViewModels.forEach { viewModel ->
            viewModel.javaClass.superclass.getDeclaredMethod("onCleared").apply {
                isAccessible = true
            }.invoke(viewModel)
        }
        createdViewModels.clear()
    }

    private fun product(
        id: String,
        title: String,
        amount: String = "10.0",
        currencyCode: String = "USD",
    ): Product {
        return Product(
            id = id,
            title = title,
            featuredImage = null,
            minPrice = Money.from(amount, currencyCode),
            maxPrice = Money.from(amount, currencyCode),
            description = null,
            productType = null,
            images = null,
            variants = null,
        )
    }
}
