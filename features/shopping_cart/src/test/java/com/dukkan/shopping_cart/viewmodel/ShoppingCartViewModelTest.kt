package com.dukkan.shopping_cart.viewmodel

import android.content.Context
import com.dukkan.domain.model.AppCurrency
import com.dukkan.domain.model.AuthUser
import com.dukkan.domain.model.cart.CartLine
import com.dukkan.domain.model.cart.StoreCart
import com.dukkan.domain.repository.SettingsRepository
import com.dukkan.domain.usecase.GetCurrentUserUseCase
import com.dukkan.domain.usecase.cart.*
import com.dukkan.domain.usecase.coupon.CouponUseCases
import com.dukkan.shopping_cart.uistate.ShoppingCartState
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import com.dukkan.shopping_cart.R

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingCartViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val getCartUseCase = mockk<GetCartUseCase>()
    private val addToCartUseCase = mockk<AddToCartUseCase>()
    private val updateCartQuantityUseCase = mockk<UpdateCartQuantityUseCase>()
    private val removeFromCartUseCase = mockk<RemoveFromCartUseCase>()
    private val createCartUseCase = mockk<CreateCartUseCase>()
    private val saveCartIdUseCase = mockk<SaveCartIdUseCase>()
    private val applyDiscountCodeUseCase = mockk<ApplyDiscountCodeUseCase>()
    private val removeDiscountCodeUseCase = mockk<RemoveDiscountCodeUseCase>()

    private val cartUseCases = CartUseCases(
        getCart = getCartUseCase,
        addToCart = addToCartUseCase,
        updateCartQuantity = updateCartQuantityUseCase,
        removeFromCart = removeFromCartUseCase,
        createCart = createCartUseCase,
        saveCartId = saveCartIdUseCase,
        applyDiscountCode = applyDiscountCodeUseCase,
        removeDiscountCode = removeDiscountCodeUseCase
    )

    @MockK
    lateinit var getCurrentUser: GetCurrentUserUseCase

    @MockK
    lateinit var couponUseCases: CouponUseCases

    @MockK
    lateinit var settingsRepository: SettingsRepository

    @MockK
    lateinit var context: Context

    private val currencyFlow = MutableSharedFlow<AppCurrency>(replay = 1)

    private lateinit var viewModel: ShoppingCartViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any<String>(), any<String>()) } returns 0
        every { android.util.Log.e(any<String>(), any<String>()) } returns 0

        // Default behavior for init
        coEvery { getCurrentUser() } returns null
        currencyFlow.tryEmit(AppCurrency.USD)
        every { settingsRepository.currency } returns currencyFlow
        every { couponUseCases.getSavedCoupon() } returns flowOf(null)
        
        // Default behavior for UseCases
        coEvery { getCartUseCase() } returns null

        viewModel = createViewModel()
    }

    private fun createViewModel() = ShoppingCartViewModel(
        cartUseCases,
        getCurrentUser,
        couponUseCases,
        settingsRepository,
        context
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(android.util.Log::class)
    }

    @Test
    fun `initial state is empty and not logged in`() = runTest {
        assertEquals(false, viewModel.isLoggedIn.value)
        assertEquals(ShoppingCartState(), viewModel.state.value)
    }

    @Test
    fun `when user is logged in, cart is loaded on init`() = runTest(testDispatcher) {
        val mockUser = mockk<AuthUser>()
        val mockCart = mockk<StoreCart>()
        
        coEvery { getCurrentUser() } returns mockUser
        coEvery { getCartUseCase() } returns mockCart
        
        viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.isLoggedIn.value)
        assertEquals(mockCart, viewModel.state.value.cart)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `currency change triggers cart refresh when logged in`() = runTest(testDispatcher) {
        val mockUser = mockk<AuthUser>()
        coEvery { getCurrentUser() } returns mockUser
        coEvery { getCartUseCase() } returns mockk()
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        coVerify(exactly = 1) { getCartUseCase() }

        // Change currency
        currencyFlow.emit(AppCurrency.EGP)
        advanceUntilIdle()

        coVerify(exactly = 2) { getCartUseCase() }
    }

    @Test
    fun `onScreenEntered refreshes cart and loads saved coupon`() = runTest(testDispatcher) {
        val mockCart = mockk<StoreCart>()
        val couponCode = "SAVE10"
        
        coEvery { getCartUseCase() } returns mockCart
        every { couponUseCases.getSavedCoupon() } returns flowOf(couponCode)
        
        viewModel.onScreenEntered()
        advanceUntilIdle()

        assertEquals(mockCart, viewModel.state.value.cart)
        assertEquals(couponCode, viewModel.state.value.promoCode)
    }

    @Test
    fun `updateQuantity updates state optimistically and then refreshes`() = runTest(testDispatcher) {
        val lineId = "line_1"
        val initialLine = mockk<CartLine> {
            every { id } returns lineId
            every { quantity } returns 1
            every { copy(quantity = 2) } returns mockk {
                every { id } returns lineId
                every { quantity } returns 2
            }
        }
        val initialCart = mockk<StoreCart> {
            every { lines } returns listOf(initialLine)
            every { copy(lines = any()) } returns this
        }
        
        coEvery { getCartUseCase() } returns initialCart
        viewModel.onScreenEntered()
        advanceUntilIdle()

        coEvery { updateCartQuantityUseCase(lineId, 2) } returns Unit
        
        viewModel.updateQuantity(initialLine, 2)
        advanceUntilIdle()
        
        coVerify { updateCartQuantityUseCase(lineId, 2) }
        coVerify(atLeast = 2) { getCartUseCase() }
    }

    @Test
    fun `updateQuantity refreshes cart even on failure`() = runTest(testDispatcher) {
        val line = mockk<CartLine>(relaxed = true) { every { id } returns "l1" }
        val cart = mockk<StoreCart>(relaxed = true) { every { lines } returns listOf(line) }
        coEvery { getCartUseCase() } returns cart
        viewModel.onScreenEntered()
        advanceUntilIdle()

        coEvery { updateCartQuantityUseCase(any(), any()) } throws Exception("Network error")
        
        viewModel.updateQuantity(line, 5)
        advanceUntilIdle()

        coVerify { getCartUseCase() }
    }

    @Test
    fun `applyPromoCode success clears coupon and reloads cart`() = runTest(testDispatcher) {
        val code = "PROMO"
        viewModel.onPromoCodeChange(code)
        
        coEvery { applyDiscountCodeUseCase(code) } returns Result.success(Unit)
        coEvery { couponUseCases.clearCoupon() } returns Unit
        coEvery { getCartUseCase() } returns mockk()

        viewModel.applyPromoCode()
        advanceUntilIdle()
        
        assertFalse(viewModel.state.value.isApplyingPromo)
        assertEquals("", viewModel.state.value.promoCode)
        assertNull(viewModel.state.value.promoError)
        
        coVerify { couponUseCases.clearCoupon() }
        coVerify { getCartUseCase() }
    }

    @Test
    fun `applyPromoCode failure shows error from exception or resource`() = runTest(testDispatcher) {
        val code = "INVALID"
        viewModel.onPromoCodeChange(code)
        
        // Scenario 1: Exception has message
        val errorMsg = "Invalid code"
        coEvery { applyDiscountCodeUseCase(code) } returns Result.failure(Exception(errorMsg))

        viewModel.applyPromoCode()
        advanceUntilIdle()
        assertEquals(errorMsg, viewModel.state.value.promoError)

        // Scenario 2: Exception has no message, use resource
        coEvery { applyDiscountCodeUseCase(code) } returns Result.failure(Exception())
        every { context.getString(R.string.invalid_promo_code) } returns "Default Error"
        
        viewModel.applyPromoCode()
        advanceUntilIdle()
        assertEquals("Default Error", viewModel.state.value.promoError)
    }

    @Test
    fun `removePromoCode reloads cart`() = runTest(testDispatcher) {
        val code = "PROMO"
        coEvery { removeDiscountCodeUseCase(code) } returns Result.success(Unit)
        coEvery { getCartUseCase() } returns mockk()

        viewModel.removePromoCode(code)
        advanceUntilIdle()
        
        assertFalse(viewModel.state.value.isApplyingPromo)
        coVerify { removeDiscountCodeUseCase(code) }
        coVerify { getCartUseCase() }
    }

    @Test
    fun `show and dismiss remove dialog`() {
        val item = mockk<CartLine>()
        viewModel.showRemoveDialog(item)
        assertEquals(item, viewModel.state.value.showRemoveDialogForItem)

        viewModel.dismissRemoveDialog()
        assertNull(viewModel.state.value.showRemoveDialogForItem)
    }

    @Test
    fun `confirmRemoveItem calls use case and reloads`() = runTest(testDispatcher) {
        val item = mockk<CartLine> { every { id } returns "line_1" }
        viewModel.showRemoveDialog(item)
        
        coEvery { removeFromCartUseCase("line_1") } returns Unit
        coEvery { getCartUseCase() } returns mockk()

        viewModel.confirmRemoveItem()
        advanceUntilIdle()

        assertNull(viewModel.state.value.showRemoveDialogForItem)
        coVerify { removeFromCartUseCase("line_1") }
        coVerify { getCartUseCase() }
    }

    @Test
    fun `onPromoCodeChange resets error`() = runTest {
        // Set error first
        coEvery { applyDiscountCodeUseCase(any()) } returns Result.failure(Exception("Error"))
        viewModel.onPromoCodeChange("BAD")
        viewModel.applyPromoCode()
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.promoError)

        // Change code
        viewModel.onPromoCodeChange("NEW")
        assertNull(viewModel.state.value.promoError)
        assertEquals("NEW", viewModel.state.value.promoCode)
    }
}
