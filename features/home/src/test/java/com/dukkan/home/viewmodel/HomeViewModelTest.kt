package com.dukkan.home.viewmodel

import app.cash.turbine.test
import com.dukkan.domain.model.AppCurrency
import com.dukkan.domain.model.AppLanguage
import com.dukkan.domain.model.AuthUser
import com.dukkan.domain.model.Brand
import com.dukkan.domain.model.Category.Category
import com.dukkan.domain.model.FavoriteProduct
import com.dukkan.domain.model.Money
import com.dukkan.domain.model.NetworkImage
import com.dukkan.domain.model.Product
import com.dukkan.domain.model.ProductSummary
import com.dukkan.domain.usecase.GetBrandsUseCase
import com.dukkan.domain.usecase.auth.GetCurrentUserUseCase
import com.dukkan.domain.usecase.category.GetCategoriesUseCase
import com.dukkan.domain.usecase.favorite.GetFavoritesUseCase
import com.dukkan.domain.usecase.favorite.ToggleFavoriteUseCase
import com.dukkan.domain.usecase.product.GetProductsUseCase
import com.dukkan.domain.usecase.settings.GetCurrencyUseCase
import com.dukkan.domain.usecase.settings.GetLanguageUseCase
import com.dukkan.home.uistate.HomeUiState
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    lateinit var getProductsUseCase: GetProductsUseCase

    @MockK
    lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase

    @MockK
    lateinit var getCategoriesUseCase: GetCategoriesUseCase

    @MockK
    lateinit var getBrandsUseCase: GetBrandsUseCase

    @MockK
    lateinit var getCurrentUserUseCase: GetCurrentUserUseCase

    @MockK
    lateinit var getFavoritesUseCase: GetFavoritesUseCase

    @MockK
    lateinit var getCurrencyUseCase: GetCurrencyUseCase

    @MockK
    lateinit var getLanguageUseCase: GetLanguageUseCase

    private val favoritesFlow = MutableStateFlow<List<FavoriteProduct>>(emptyList())
    private val currencyFlow = MutableStateFlow(AppCurrency.USD)
    private val languageFlow = MutableStateFlow(AppLanguage.ENGLISH)
    private val categoriesFlow = MutableStateFlow<List<Category>>(emptyList())
    private val brandsFlow = MutableStateFlow<List<Brand>>(emptyList())
    private val createdViewModels = mutableListOf<Any>()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        stubDefaults()
    }

    @After
    fun tearDown() {
        clearCreatedViewModels()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() {
        val viewModel = createViewModel()
        assertEquals(HomeUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `loadInitialProducts success transitions to Success`() = runTest(testDispatcher) {
        val products = listOf(
            product(id = "p1", title = "Product 1"),
            product(id = "p2", title = "Product 2")
        )
        val favoriteIds = listOf(FavoriteProduct("p1", "Product 1", "", "10.00", "USD"))
        val categories = listOf(Category(id = "cat-1", name = "Category", handle = "category"))
        val brands = listOf(Brand(id = "brand-1", name = "Brand"))

        stubHomeData(
            productResults = listOf(products),
            categories = categories,
            brands = brands,
            favorites = favoriteIds,
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(HomeUiState.Loading, awaitItem())
            advanceUntilIdle()

            val state = awaitItem()
            assertTrue(state is HomeUiState.Success)
            state as HomeUiState.Success
            assertEquals(products, state.products)
            assertEquals(setOf("p1"), state.favoriteIds)
            assertEquals(listOf("Category"), state.categories)
            assertEquals(brands, state.brands)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadInitialProducts failure transitions to Error`() = runTest(testDispatcher) {
        stubHomeData(
            productException = Exception("Network Error"),
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(HomeUiState.Loading, awaitItem())
            advanceUntilIdle()

            val state = awaitItem()
            assertTrue(state is HomeUiState.Error)
            assertEquals("Network Error", (state as HomeUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadCurrentUser updates firstName`() = runTest(testDispatcher) {
        stubHomeData(currentUser = AuthUser("uid", "john@example.com", "John"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("John", viewModel.firstName.value)
        coVerify { getCurrentUserUseCase() }
    }

    @Test
    fun `loadCurrentUser exception leaves firstName null`() = runTest(testDispatcher) {
        stubHomeData(currentUserException = Exception("boom"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertNull(viewModel.firstName.value)
        coVerify { getCurrentUserUseCase() }
    }

    @Test
    fun `currency change reloads products`() = runTest(testDispatcher) {
        val firstPage = listOf(product(id = "p1", title = "Product 1"))
        val secondPage = listOf(product(id = "p2", title = "Product 2"))
        stubHomeData(productResults = listOf(firstPage, secondPage))

        createViewModel()
        advanceUntilIdle()

        coVerify(exactly = 1) { getProductsUseCase(limit = 10) }

        currencyFlow.value = AppCurrency.EGP
        advanceUntilIdle()

        coVerify(exactly = 2) { getProductsUseCase(limit = 10) }
    }

    @Test
    fun `language change reloads products`() = runTest(testDispatcher) {
        val firstPage = listOf(product(id = "p1", title = "Product 1"))
        val secondPage = listOf(product(id = "p2", title = "Product 2"))
        stubHomeData(productResults = listOf(firstPage, secondPage))

        createViewModel()
        advanceUntilIdle()

        coVerify(exactly = 1) { getProductsUseCase(limit = 10) }

        languageFlow.value = AppLanguage.ARABIC
        advanceUntilIdle()

        coVerify(exactly = 2) { getProductsUseCase(limit = 10) }
    }

    @Test
    fun `loadMoreProducts appends when pagination is available`() = runTest(testDispatcher) {
        val firstPage = listOf(product(id = "p1", title = "Product 1"))
        val secondPage = listOf(product(id = "p2", title = "Product 2"))
        stubHomeData(productResults = listOf(firstPage, secondPage))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(HomeUiState.Loading, awaitItem())
            advanceUntilIdle()

            val initialState = awaitItem() as HomeUiState.Success
            assertEquals(firstPage, initialState.products)

            viewModel.loadMoreProducts()
            advanceUntilIdle()
            coVerify(exactly = 1) { getProductsUseCase(limit = 10) }
            expectNoEvents()

            setPrivateBoolean(viewModel, "hasNextPage", true)
            viewModel.loadMoreProducts()
            advanceUntilIdle()

            coVerify(exactly = 2) { getProductsUseCase(limit = 10) }
            assertEquals(firstPage + secondPage, (awaitItem() as HomeUiState.Success).products)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onFavoriteClick for guest sends NavigateToFavoritesGuest event`() = runTest(testDispatcher) {
        stubHomeData(currentUser = null)
        val viewModel = createViewModel()
        val product = product(id = "p1", title = "Guest Product")

        viewModel.events.test {
            viewModel.onFavoriteClick(product, false)
            assertEquals(HomeEvent.NavigateToFavoritesGuest, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onFavoriteClick for logged in user calls toggleFavoriteUseCase`() = runTest(testDispatcher) {
        stubHomeData(currentUser = AuthUser("uid", "john@example.com", "John"))
        val viewModel = createViewModel()
        val product = product(id = "p1", title = "Title")
        val favoriteSlot = slot<FavoriteProduct>()

        coEvery { toggleFavoriteUseCase(capture(favoriteSlot), false) } returns Unit

        viewModel.onFavoriteClick(product, false)
        advanceUntilIdle()

        coVerify(exactly = 1) { toggleFavoriteUseCase(any(), false) }
        assertEquals(
            FavoriteProduct(
                id = "p1",
                title = "Title",
                imageUrl = "",
                price = "10.0",
                currencyCode = "USD",
            ),
            favoriteSlot.captured
        )
    }

    private fun createViewModel(): HomeViewModel {
        return HomeViewModel(
            getProductsUseCase,
            toggleFavoriteUseCase,
            getCategoriesUseCase,
            getBrandsUseCase,
            getCurrentUserUseCase,
            getFavoritesUseCase,
            getCurrencyUseCase,
            getLanguageUseCase,
        ).also { createdViewModels += it }
    }

    private fun stubDefaults() {
        stubHomeData()
    }

    private fun stubHomeData(
        productResults: List<List<Product>> = listOf(emptyList()),
        categories: List<Category> = emptyList(),
        brands: List<Brand> = emptyList(),
        favorites: List<FavoriteProduct> = emptyList(),
        currentUser: AuthUser? = null,
        currentUserException: Throwable? = null,
        productException: Throwable? = null,
    ) {
        favoritesFlow.value = favorites
        currencyFlow.value = AppCurrency.USD
        languageFlow.value = AppLanguage.ENGLISH
        categoriesFlow.value = categories
        brandsFlow.value = brands

        if (currentUserException != null) {
            coEvery { getCurrentUserUseCase() } throws currentUserException
        } else {
            coEvery { getCurrentUserUseCase() } returns currentUser
        }

        if (productException != null) {
            coEvery { getProductsUseCase(limit = 10) } throws productException
        } else if (productResults.size == 1) {
            coEvery { getProductsUseCase(limit = 10) } returns productResults.single()
        } else {
            coEvery { getProductsUseCase(limit = 10) } returnsMany productResults
        }

        every { getFavoritesUseCase() } returns favoritesFlow
        every { getCurrencyUseCase() } returns currencyFlow
        every { getLanguageUseCase() } returns languageFlow
        every { getCategoriesUseCase() } returns categoriesFlow
        every { getBrandsUseCase() } returns brandsFlow
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

    private fun setPrivateBoolean(viewModel: HomeViewModel, fieldName: String, value: Boolean) {
        val field = viewModel.javaClass.getDeclaredField(fieldName).apply {
            isAccessible = true
        }
        field.setBoolean(viewModel, value)
    }

    private fun clearCreatedViewModels() {
        createdViewModels.forEach { viewModel ->
            viewModel.javaClass.superclass.getDeclaredMethod("onCleared").apply {
                isAccessible = true
            }.invoke(viewModel)
        }
        createdViewModels.clear()
    }
}
