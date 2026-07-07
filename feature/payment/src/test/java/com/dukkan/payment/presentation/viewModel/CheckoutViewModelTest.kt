package com.dukkan.payment.presentation.viewModel

import androidx.lifecycle.SavedStateHandle
import com.dukkan.domain.model.Address
import com.dukkan.domain.model.Money
import com.dukkan.domain.model.OrderConfirmation
import com.dukkan.domain.model.cart.StoreCart
import com.dukkan.domain.usecase.address.GetAddressesUseCase
import com.dukkan.domain.usecase.cart.GetCartUseCase
import com.dukkan.domain.usecase.customer.GetCustomerIdUseCase
import com.dukkan.payment.PaymentResult
import com.dukkan.payment.domain.model.*
import com.dukkan.payment.domain.usecase.*
import com.dukkan.domain.usecase.cart.ClearCartFullyUseCase
import com.dukkan.payment.presentation.CheckoutConstants
import com.dukkan.payment.presentation.uiState.CheckoutEvent
import com.dukkan.payment.presentation.uiState.OrderResult
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import app.cash.turbine.test

@OptIn(ExperimentalCoroutinesApi::class)
internal class CheckoutViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    lateinit var createPaymentIntentionUseCase: CreatePaymentIntentionUseCase
    @MockK
    lateinit var verifyPaymentStatusUseCase: VerifyPaymentStatusUseCase
    @MockK
    lateinit var createOrderUseCase: CreateOrderUseCase
    @MockK
    lateinit var markOrderPaidUseCase: MarkOrderPaidUseCase
    @MockK
    lateinit var deleteOrderUseCase: DeleteOrderUseCase
    @MockK
    lateinit var clearCartFullyUseCase: ClearCartFullyUseCase
    @MockK
    lateinit var getCustomerIdUseCase: GetCustomerIdUseCase
    @MockK
    lateinit var getCartUseCase: GetCartUseCase
    @MockK
    lateinit var getAddressesUseCase: GetAddressesUseCase

    private lateinit var viewModel: CheckoutViewModel
    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        savedStateHandle = SavedStateHandle()
        
        // Defaults for init
        coEvery { getCartUseCase() } returns null
        coEvery { getAddressesUseCase() } returns Result.success(emptyList())

        viewModel = createViewModel()
    }

    private fun createViewModel() = CheckoutViewModel(
        savedStateHandle,
        createPaymentIntentionUseCase,
        verifyPaymentStatusUseCase,
        createOrderUseCase,
        markOrderPaidUseCase,
        deleteOrderUseCase,
        clearCartFullyUseCase,
        getCustomerIdUseCase,
        getCartUseCase,
        getAddressesUseCase
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load fetches cart and addresses`() = runTest {
        val mockCart = mockk<StoreCart>(relaxed = true) {
            every { id } returns "cart_1"
            every { cost.totalAmount } returns Money(BigDecimal("100.0"), "EGP")
            every { lines } returns listOf(mockk(), mockk())
        }
        val mockAddresses = listOf(
            Address(id = "addr_1", isDefault = true, firstName = "John"),
            Address(id = "addr_2", isDefault = false, firstName = "Jane")
        )
        
        coEvery { getCartUseCase() } returns mockCart
        coEvery { getAddressesUseCase() } returns Result.success(mockAddresses)

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("cart_1", state.cartSummary?.cartId)
        assertEquals(2, state.cartSummary?.lineCount)
        assertEquals(2, state.addresses.size)
        // Should select default address
        assertEquals("addr_1", (state.selectedAddress as? CheckoutAddress.Saved)?.addressId)
    }

    @Test
    fun `SelectAddress updates state`() = runTest {
        val newAddress = CheckoutAddress.Saved("addr_new")
        viewModel.onEvent(CheckoutEvent.SelectAddress(newAddress))
        assertEquals(newAddress, viewModel.uiState.value.selectedAddress)
    }

    @Test
    fun `SelectMethod updates state`() = runTest {
        viewModel.onEvent(CheckoutEvent.SelectMethod(PaymentMethod.CASH))
        assertEquals(PaymentMethod.CASH, viewModel.uiState.value.selectedMethod)
    }

    @Test
    fun `submitOrder for CASH success clears cart and finishes with Success`() = runTest {
        val cart = mockk<StoreCart>(relaxed = true) {
            every { id } returns "cart_1"
            every { cost.totalAmount } returns Money(BigDecimal("100.0"), "EGP")
        }
        coEvery { getCartUseCase() } returns cart
        coEvery { getAddressesUseCase() } returns Result.success(listOf(Address(id = "addr_1")))
        coEvery { getCustomerIdUseCase() } returns Result.success("cust_1")
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        viewModel.onEvent(CheckoutEvent.SelectMethod(PaymentMethod.CASH))
        
        val createdOrder = CreatedOrder("ord_1", "PENDING", Money(BigDecimal("100.0"), "EGP"))
        coEvery { createOrderUseCase(any(), any()) } returns Result.success(createdOrder)
        coEvery { clearCartFullyUseCase() } returns Unit

        viewModel.effect.test {
            viewModel.onEvent(CheckoutEvent.SubmitOrder)
            
            val effect = awaitItem()
            assertTrue(effect is CheckoutEffect.Finish)
            val result = (effect as CheckoutEffect.Finish).result
            assertTrue(result is PaymentResult.Success)
            assertEquals("ord_1", (result as PaymentResult.Success).orderId)
            
            coVerify { clearCartFullyUseCase() }
        }
    }

    @Test
    fun `submitOrder for ONLINE failure during order creation shows error`() = runTest {
        val cart = mockk<StoreCart>(relaxed = true)
        coEvery { getCartUseCase() } returns cart
        coEvery { getAddressesUseCase() } returns Result.success(listOf(Address(id = "addr_1")))
        coEvery { getCustomerIdUseCase() } returns Result.success("cust_1")
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        viewModel.onEvent(CheckoutEvent.SelectMethod(PaymentMethod.ONLINE))
        
        coEvery { createOrderUseCase(any(), any()) } returns Result.failure(Exception("Creation Failed"))

        viewModel.onEvent(CheckoutEvent.SubmitOrder)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.result is OrderResult.Failure)
    }

    @Test
    fun `cancelPendingOrder deletes order and shows failure`() = runTest {
        // Set an orderId in savedStateHandle or via mock
        savedStateHandle[CheckoutConstants.KEY_ORDER_ID] = "ord_1"
        viewModel = createViewModel()
        
        coEvery { deleteOrderUseCase("ord_1") } returns Result.success(Unit)

        viewModel.onEvent(CheckoutEvent.CancelPaymentFlow)
        advanceUntilIdle()

        coVerify { deleteOrderUseCase("ord_1") }
        assertTrue(viewModel.uiState.value.result is OrderResult.Failure)
    }

    @Test
    fun `online payment success flow with polling`() = runTest {
        val cart = mockk<StoreCart>(relaxed = true) {
            every { id } returns "cart_1"
            every { cost.totalAmount } returns Money(BigDecimal("100.0"), "EGP")
        }
        coEvery { getCartUseCase() } returns cart
        coEvery { getAddressesUseCase() } returns Result.success(listOf(Address(id = "addr_1")))
        coEvery { getCustomerIdUseCase() } returns Result.success("cust_1")
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        viewModel.onEvent(CheckoutEvent.SelectMethod(PaymentMethod.ONLINE))
        
        val createdOrder = CreatedOrder("ord_1", "PENDING", Money(BigDecimal("100.0"), "EGP"))
        coEvery { createOrderUseCase(any(), any()) } returns Result.success(createdOrder)
        coEvery { createPaymentIntentionUseCase(any(), any(), any(), any()) } returns Result.success(mockk(relaxed = true))

        viewModel.onEvent(CheckoutEvent.SubmitOrder)
        advanceUntilIdle()
        
        assertEquals("ord_1", savedStateHandle[CheckoutConstants.KEY_ORDER_ID])

        // Mock verification status polling: first pending, then success
        val pendingConfirmation = OrderConfirmation("ord_1", "pending", Money(BigDecimal("100.0"), "EGP"))
        val successConfirmation = OrderConfirmation("ord_1", "paid", Money(BigDecimal("100.0"), "EGP"))
        
        coEvery { verifyPaymentStatusUseCase("ord_1", any()) } returnsMany listOf(
            Result.success(pendingConfirmation),
            Result.success(successConfirmation)
        )
        coEvery { markOrderPaidUseCase("ord_1") } returns Result.success(createdOrder)
        coEvery { clearCartFullyUseCase() } returns Unit

        // Resume app to trigger verification
        viewModel.onEvent(CheckoutEvent.AppResumedDuringPayment)
        
        // We need to advance time because of delay(POLL_DELAY_MS)
        advanceTimeBy(CheckoutConstants.POLL_DELAY_MS * 2)
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.result is OrderResult.Success)
        coVerify(atLeast = 2) { verifyPaymentStatusUseCase("ord_1", any()) }
        coVerify { markOrderPaidUseCase("ord_1") }
    }
}
