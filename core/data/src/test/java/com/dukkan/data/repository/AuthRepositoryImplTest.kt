package com.dukkan.data.repository

import com.apollographql.apollo.ApolloClient
import com.dukkan.data.source.local.ShopifyTokenStore
import com.dukkan.data.source.remote.FirebaseAuthDataSource
import com.dukkan.data.source.remote.IFirebaseStoreDataSource
import com.dukkan.data.source.remote.ShopifyAuthDataSource
import com.dukkan.data.source.remote.dto.UserAuthDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import android.util.Log
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

class AuthRepositoryImplTest {

    private lateinit var repository: AuthRepositoryImpl
    private val firebaseAuthDataSource: FirebaseAuthDataSource = mockk()
    private val firebaseStoreDataSource: IFirebaseStoreDataSource = mockk()
    private val shopifyAuthDataSource: ShopifyAuthDataSource = mockk()
    private val shopifyTokenStore: ShopifyTokenStore = mockk(relaxed = true)
    private val apolloClient: ApolloClient = mockk()

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.e(any<String>(), any<String>(), any<Throwable>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.d(any<String>(), any<String>()) } returns 0
        repository = AuthRepositoryImpl(
            firebaseAuthDataSource,
            firebaseStoreDataSource,
            shopifyAuthDataSource,
            shopifyTokenStore,
            apolloClient
        )
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `login success returns AuthUser`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        val userDto = UserAuthDto("uid", email, "Test User")
        coEvery { firebaseAuthDataSource.loginWithEmailAndPassword(email, password) } returns userDto
        coEvery { shopifyAuthDataSource.createCustomerToken(email, password) } returns null

        // When
        val result = repository.login(email, password)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("uid", result.getOrNull()?.uid)
        assertEquals(email, result.getOrNull()?.email)
        coVerify { firebaseAuthDataSource.loginWithEmailAndPassword(email, password) }
    }

    @Test
    fun `login failure returns Result failure`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        val exception = Exception("Login failed")
        coEvery { firebaseAuthDataSource.loginWithEmailAndPassword(email, password) } throws exception

        // When
        val result = repository.login(email, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `register success returns AuthUser`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        val firstName = "First"
        val lastName = "Last"
        val userDto = UserAuthDto("uid", email, "First Last")
        
        coEvery { 
            firebaseAuthDataSource.registerWithEmailAndPassword(email, password, "First Last") 
        } returns userDto
        coEvery { firebaseStoreDataSource.saveUser(userDto) } returns Unit
        coEvery { 
            shopifyAuthDataSource.createShopifyCustomer(email, password, firstName, lastName) 
        } returns true
        coEvery { shopifyAuthDataSource.createCustomerToken(email, password) } returns null

        // When
        val result = repository.register(email, password, firstName, lastName)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("uid", result.getOrNull()?.uid)
        coVerify { firebaseAuthDataSource.registerWithEmailAndPassword(email, password, "First Last") }
        coVerify { firebaseStoreDataSource.saveUser(userDto) }
        coVerify { shopifyAuthDataSource.createShopifyCustomer(email, password, firstName, lastName) }
    }

    @Test
    fun `signOut clears local storage`() = runTest {
        // Given
        coEvery { firebaseAuthDataSource.signOut() } returns Unit
        coEvery { shopifyTokenStore.clearToken() } returns Unit

        // When
        repository.signOut()

        // Then
        coVerify { firebaseAuthDataSource.signOut() }
        coVerify { shopifyTokenStore.clearToken() }
    }
}
