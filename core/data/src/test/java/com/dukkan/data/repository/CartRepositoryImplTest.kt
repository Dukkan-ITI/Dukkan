package com.dukkan.data.repository

import com.dukkan.GetCartQuery
import com.dukkan.AddCartLinesMutation
import com.dukkan.ApplyDiscountCodeMutation
import com.dukkan.CreateCartMutation
import com.dukkan.UpdateCartLinesMutation
import com.dukkan.RemoveCartLinesMutation
import com.dukkan.fragment.MoneyFields
import com.dukkan.data.source.local.ShopifyTokenStore
import com.dukkan.data.source.local.data_source.cart.CartLocalDataSource
import com.dukkan.data.source.remote.data_source.cart.CartFirestoreDataSource
import com.dukkan.data.source.remote.data_source.cart.CartRemoteDataSource
import com.dukkan.domain.model.AppCurrency
import com.dukkan.domain.model.cart.StoreCart
import com.dukkan.domain.repository.SettingsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

class CartRepositoryImplTest {

    @MockK
    lateinit var localDataSource: CartLocalDataSource

    @MockK
    lateinit var remoteDataSource: CartRemoteDataSource

    @MockK
    lateinit var firestoreDataSource: CartFirestoreDataSource

    @MockK
    lateinit var tokenStore: ShopifyTokenStore

    @MockK
    lateinit var firebaseAuth: FirebaseAuth

    @MockK
    lateinit var settingsRepository: SettingsRepository

    private lateinit var repository: CartRepositoryImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        // Mock static Log
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any<String>(), any<String>()) } returns 0
        every { android.util.Log.w(any<String>(), any<String>()) } returns 0
        every { android.util.Log.w(any<String>(), any<String>(), any<Throwable>()) } returns 0
        every { android.util.Log.e(any<String>(), any<String>()) } returns 0
        every { android.util.Log.e(any<String>(), any<String>(), any<Throwable>()) } returns 0

        repository = CartRepositoryImpl(
            localDataSource,
            remoteDataSource,
            firestoreDataSource,
            tokenStore,
            firebaseAuth,
            settingsRepository
        )
    }

    @After
    fun tearDown() {
        unmockkStatic(android.util.Log::class)
    }

    private fun mockMoney(amountStr: String): MoneyFields {
        val mockMoneyFields = mockk<MoneyFields>()
        every { mockMoneyFields.amount } returns amountStr
        every { mockMoneyFields.currencyCode.rawValue } returns "USD"
        return mockMoneyFields
    }

    private fun mockRemoteCart(cartId: String): GetCartQuery.Cart {
        val mockCart = mockk<GetCartQuery.Cart>()
        every { mockCart.id } returns cartId
        every { mockCart.checkoutUrl } returns "https://checkout.com"
        every { mockCart.totalQuantity } returns 1
        every { mockCart.discountCodes } returns emptyList()
        every { mockCart.cost } returns mockk {
            every { subtotalAmount.moneyFields } returns mockMoney("100.0")
            every { totalAmount.moneyFields } returns mockMoney("110.0")
            every { totalTaxAmount } returns null
            every { checkoutChargeAmount.moneyFields } returns mockMoney("110.0")
        }
        every { mockCart.lines.edges } returns emptyList()
        return mockCart
    }

    // --- getCart Tests ---

    @Test
    fun `getCart returns null if no cart ID in local storage`() = runTest {
        every { settingsRepository.currency } returns flowOf(AppCurrency.USD)
        coEvery { localDataSource.getCartId() } returns null

        val result = repository.getCart()

        assertNull(result)
        coVerify(exactly = 1) { localDataSource.getCartId() }
    }

    @Test
    fun `getCart returns cart from remote and caches it`() = runTest {
        val cartId = "cart_123"
        val country = AppCurrency.USD.countryCode
        val mockCart = mockRemoteCart(cartId)
        
        every { settingsRepository.currency } returns flowOf(AppCurrency.USD)
        coEvery { localDataSource.getCartId() } returns cartId
        coEvery { remoteDataSource.getCart(cartId, country) } returns mockCart

        val result1 = repository.getCart()
        val result2 = repository.getCart()

        assertNotNull(result1)
        assertEquals(cartId, result1?.id)
        assertEquals(result1, result2)
        coVerify(exactly = 1) { remoteDataSource.getCart(cartId, country) }
    }

    @Test
    fun `getCart returns null if remote returns null`() = runTest {
        val cartId = "cart_123"
        every { settingsRepository.currency } returns flowOf(AppCurrency.USD)
        coEvery { localDataSource.getCartId() } returns cartId
        coEvery { remoteDataSource.getCart(cartId, any()) } returns null

        val result = repository.getCart()

        assertNull(result)
    }

    @Test
    fun `getCart refreshes cache when currency changes`() = runTest {
        val cartId = "cart_123"
        val mockCartUSD = mockRemoteCart(cartId)
        val mockCartEGP = mockRemoteCart(cartId)

        coEvery { localDataSource.getCartId() } returns cartId
        
        // Initial USD call
        every { settingsRepository.currency } returns flowOf(AppCurrency.USD)
        coEvery { remoteDataSource.getCart(cartId, AppCurrency.USD.countryCode) } returns mockCartUSD
        repository.getCart()

        // Change to EGP
        every { settingsRepository.currency } returns flowOf(AppCurrency.EGP)
        coEvery { remoteDataSource.getCart(cartId, AppCurrency.EGP.countryCode) } returns mockCartEGP

        val result = repository.getCart()

        assertNotNull(result)
        coVerify(exactly = 1) { remoteDataSource.getCart(cartId, AppCurrency.USD.countryCode) }
        coVerify(exactly = 1) { remoteDataSource.getCart(cartId, AppCurrency.EGP.countryCode) }
    }

    // --- addCartItem Tests ---

    @Test
    fun `addCartItem creates new cart if no ID exists`() = runTest {
        val variantId = "var_1"
        val newCartId = "new_cart_id"
        val userId = "user_1"
        
        coEvery { localDataSource.getCartId() } returns null
        coEvery { tokenStore.getToken() } returns null
        coEvery { remoteDataSource.createCart(null) } returns mockk {
            every { cart?.id } returns newCartId
        }
        coEvery { localDataSource.saveCartId(newCartId) } returns Unit
        every { firebaseAuth.currentUser?.uid } returns userId
        coEvery { firestoreDataSource.saveCartId(newCartId, userId) } returns Unit
        coEvery { remoteDataSource.addCartItem(newCartId, variantId) } returns mockk(relaxed = true)

        repository.addCartItem(variantId)

        coVerify { remoteDataSource.createCart(null) }
        coVerify { localDataSource.saveCartId(newCartId) }
        coVerify { firestoreDataSource.saveCartId(newCartId, userId) }
        coVerify { remoteDataSource.addCartItem(newCartId, variantId) }
    }

    @Test
    fun `addCartItem uses existing ID and logs error if add fails`() = runTest {
        val variantId = "var_1"
        val cartId = "cart_123"
        coEvery { localDataSource.getCartId() } returns cartId
        coEvery { remoteDataSource.addCartItem(cartId, variantId) } returns mockk {
            every { userErrors } returns listOf(mockk { every { message } returns "Error" })
        }

        repository.addCartItem(variantId)

        coVerify { remoteDataSource.addCartItem(cartId, variantId) }
        verify { android.util.Log.e(any<String>(), any<String>()) }
    }

    @Test
    fun `addCartItem handles Firestore failure gracefully`() = runTest {
        val variantId = "var_1"
        val newCartId = "new_cart_id"
        
        coEvery { localDataSource.getCartId() } returns null
        coEvery { tokenStore.getToken() } returns null
        coEvery { remoteDataSource.createCart(any()) } returns mockk { every { cart?.id } returns newCartId }
        coEvery { localDataSource.saveCartId(newCartId) } returns Unit
        every { firebaseAuth.currentUser?.uid } returns "user_1"
        coEvery { firestoreDataSource.saveCartId(any(), any()) } throws Exception("Firestore Error")
        coEvery { remoteDataSource.addCartItem(any(), any()) } returns mockk(relaxed = true)

        repository.addCartItem(variantId)

        coVerify { localDataSource.saveCartId(newCartId) }
        verify { android.util.Log.w(any<String>(), any<String>(), any<Throwable>()) }
    }

    // --- updateCartItemQuantity Tests ---

    @Test
    fun `updateCartItemQuantity removes item if quantity is zero or less`() = runTest {
        val cartId = "cart_1"
        val lineId = "line_1"
        coEvery { localDataSource.getCartId() } returns cartId
        coEvery { remoteDataSource.removeCartItem(cartId, lineId) } returns mockk(relaxed = true)

        repository.updateCartItemQuantity(lineId, 0)
        repository.updateCartItemQuantity(lineId, -1)

        coVerify(exactly = 2) { remoteDataSource.removeCartItem(cartId, lineId) }
    }

    @Test
    fun `updateCartItemQuantity updates quantity if positive`() = runTest {
        val cartId = "cart_1"
        val lineId = "line_1"
        coEvery { localDataSource.getCartId() } returns cartId
        coEvery { remoteDataSource.updateCartItem(cartId, lineId, 5) } returns mockk(relaxed = true)

        repository.updateCartItemQuantity(lineId, 5)

        coVerify { remoteDataSource.updateCartItem(cartId, lineId, 5) }
    }

    // --- applyDiscountCode Tests ---

    @Test
    fun `applyDiscountCode returns success when applicable`() = runTest {
        val cartId = "cart_1"
        val code = "VALID"
        coEvery { localDataSource.getCartId() } returns cartId
        every { settingsRepository.currency } returns flowOf(AppCurrency.USD)
        
        coEvery { remoteDataSource.getCart(cartId, any()) } returns mockRemoteCart(cartId)
        
        val mockDiscountCode = mockk<ApplyDiscountCodeMutation.DiscountCode>()
        every { mockDiscountCode.code } returns "VALID"
        every { mockDiscountCode.applicable } returns true
        
        coEvery { remoteDataSource.applyDiscountCodes(cartId, listOf(code)) } returns mockk(relaxed = true) {
            every { userErrors } returns emptyList()
            every { cart?.discountCodes } returns listOf(mockDiscountCode)
        }

        val result = repository.applyDiscountCode(code)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `applyDiscountCode returns failure when remote call fails`() = runTest {
        coEvery { localDataSource.getCartId() } returns "cart_1"
        every { settingsRepository.currency } returns flowOf(AppCurrency.USD)
        coEvery { remoteDataSource.getCart(any(), any()) } returns mockRemoteCart("cart_1")
        coEvery { remoteDataSource.applyDiscountCodes(any(), any()) } returns null

        val result = repository.applyDiscountCode("CODE")

        assertTrue(result.isFailure)
    }

    @Test
    fun `applyDiscountCode returns failure when user errors present`() = runTest {
        coEvery { localDataSource.getCartId() } returns "cart_1"
        every { settingsRepository.currency } returns flowOf(AppCurrency.USD)
        coEvery { remoteDataSource.getCart(any(), any()) } returns mockRemoteCart("cart_1")
        coEvery { remoteDataSource.applyDiscountCodes(any(), any()) } returns mockk {
            every { userErrors } returns listOf(mockk { every { message } returns "Invalid code" })
        }

        val result = repository.applyDiscountCode("INVALID")

        assertTrue(result.isFailure)
        assertEquals("Invalid code", result.exceptionOrNull()?.message)
    }

    // --- syncCartOnLogin Tests ---

    @Test
    fun `syncCartOnLogin handles exceptions gracefully`() = runTest {
        coEvery { localDataSource.deleteCartId() } throws Exception("DataStore Error")
        
        repository.syncCartOnLogin("user_1")

        verify { android.util.Log.e(any<String>(), any<String>(), any<Throwable>()) }
    }

    @Test
    fun `syncCartOnLogin does nothing if remote ID is null`() = runTest {
        val userId = "user_1"
        coEvery { localDataSource.deleteCartId() } returns Unit
        coEvery { firestoreDataSource.getCartId(userId) } returns null
        
        repository.syncCartOnLogin(userId)

        coVerify(exactly = 0) { localDataSource.saveCartId(any()) }
    }

    // --- clearCartFully Tests ---

    @Test
    fun `clearCartFully clears both local and Firestore for logged in user`() = runTest {
        val userId = "user_1"
        every { firebaseAuth.currentUser?.uid } returns userId
        coEvery { localDataSource.deleteCartId() } returns Unit
        coEvery { firestoreDataSource.deleteCartId(userId) } returns Unit

        repository.clearCartFully()

        coVerify { localDataSource.deleteCartId() }
        coVerify { firestoreDataSource.deleteCartId(userId) }
    }

    @Test
    fun `clearCartFully only clears local for guest user`() = runTest {
        every { firebaseAuth.currentUser } returns null
        coEvery { localDataSource.deleteCartId() } returns Unit

        repository.clearCartFully()

        coVerify { localDataSource.deleteCartId() }
        coVerify(exactly = 0) { firestoreDataSource.deleteCartId(any()) }
    }

    @Test
    fun `clearCartFully handles exceptions`() = runTest {
        every { firebaseAuth.currentUser } returns null
        coEvery { localDataSource.deleteCartId() } throws Exception("Error")

        repository.clearCartFully()

        verify { android.util.Log.e(any<String>(), any<String>(), any<Throwable>()) }
    }

    // --- removeDiscountCode Tests ---
    
    @Test
    fun `removeDiscountCode success`() = runTest {
        val cartId = "cart_1"
        val codeToRemove = "PROMO"
        coEvery { localDataSource.getCartId() } returns cartId
        every { settingsRepository.currency } returns flowOf(AppCurrency.USD)
        
        val mockCart = mockRemoteCart(cartId)
        val mockDC = mockk<GetCartQuery.DiscountCode> {
            every { code } returns "PROMO"
        }
        every { mockCart.discountCodes } returns listOf(mockDC)
        
        coEvery { remoteDataSource.getCart(cartId, any()) } returns mockCart
        coEvery { remoteDataSource.applyDiscountCodes(cartId, emptyList()) } returns mockk(relaxed = true) {
            every { userErrors } returns emptyList()
        }

        val result = repository.removeDiscountCode(codeToRemove)

        assertTrue(result.isSuccess)
        coVerify { remoteDataSource.applyDiscountCodes(cartId, emptyList()) }
    }
}
