package com.dukkan.auth.viewmodel

import android.content.Context
import com.dukkan.domain.model.AuthUser
import com.dukkan.domain.usecase.GetShopifyTokenUseCase
import com.dukkan.domain.usecase.LoginUseCase
import com.dukkan.domain.usecase.LoginWithGoogleUseCase
import com.dukkan.domain.usecase.RegisterUseCase
import com.dukkan.domain.usecase.cart.SyncCartOnLoginUseCase
import com.dukkan.domain.usecase.favorite.SyncFavoritesOnLoginUseCase
import com.dukkan.domain.util.NetworkMonitor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.android.gms.tasks.Tasks
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.*
import app.cash.turbine.test
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    lateinit var context: Context

    @MockK
    lateinit var loginUseCase: LoginUseCase

    @MockK
    lateinit var registerUseCase: RegisterUseCase

    @MockK
    lateinit var loginWithGoogleUseCase: LoginWithGoogleUseCase

    @MockK
    lateinit var getShopifyTokenUseCase: GetShopifyTokenUseCase

    @MockK
    lateinit var syncFavoritesOnLoginUseCase: SyncFavoritesOnLoginUseCase

    @MockK
    lateinit var syncCartOnLoginUseCase: SyncCartOnLoginUseCase

    @MockK
    lateinit var networkMonitor: NetworkMonitor

    @MockK
    lateinit var firebaseAuth: FirebaseAuth

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        mockkStatic(FirebaseAuth::class)
        mockkStatic(android.util.Log::class)
        every { android.util.Log.e(any<String>(), any<String>(), any<Throwable>()) } returns 0
        every { android.util.Log.e(any<String>(), any<String>()) } returns 0
        every { android.util.Log.w(any<String>(), any<String>()) } returns 0
        every { android.util.Log.d(any<String>(), any<String>()) } returns 0
        
        every { FirebaseAuth.getInstance() } returns firebaseAuth
        every { firebaseAuth.signOut() } just Runs
        every { networkMonitor.isOnline } returns flowOf(true)

        viewModel = AuthViewModel(
            context,
            loginUseCase,
            registerUseCase,
            loginWithGoogleUseCase,
            getShopifyTokenUseCase,
            syncFavoritesOnLoginUseCase,
            syncCartOnLoginUseCase,
            networkMonitor
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(FirebaseAuth::class)
        unmockkStatic(android.util.Log::class)
    }

    @Test
    fun `initial state is Form in login mode`() {
        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Form)
        assertTrue((state as AuthUiState.Form).isLoginMode)
    }

    @Test
    fun `toggleMode switches between login and register`() {
        // Initial is login
        assertTrue((viewModel.uiState.value as AuthUiState.Form).isLoginMode)

        // Toggle to register
        viewModel.onAction(AuthAction.ToggleMode)
        assertFalse((viewModel.uiState.value as AuthUiState.Form).isLoginMode)

        // Toggle back to login
        viewModel.onAction(AuthAction.ToggleMode)
        assertTrue((viewModel.uiState.value as AuthUiState.Form).isLoginMode)
    }

    @Test
    fun `email and password changes update state`() {
        viewModel.onAction(AuthAction.EmailChanged("test@example.com"))
        viewModel.onAction(AuthAction.PasswordChanged("123456"))

        val state = viewModel.uiState.value as AuthUiState.Form
        assertEquals("test@example.com", state.email)
        assertEquals("123456", state.password)
    }

    @Test
    fun `submit in login mode success transitions to Success`() = runTest(testDispatcher) {
        val email = "test@example.com"
        val password = "password123"
        viewModel.onAction(AuthAction.EmailChanged(email))
        viewModel.onAction(AuthAction.PasswordChanged(password))

        val mockUser = mockk<FirebaseUser>(relaxed = true) {
            every { isEmailVerified } returns true
            every { reload() } returns Tasks.forResult(null)
            every { uid } returns "user_id"
        }
        every { firebaseAuth.currentUser } returns mockUser
        
        coEvery { loginUseCase(email, password) } returns Result.success(mockk(relaxed = true))
        coEvery { getShopifyTokenUseCase() } returns mockk(relaxed = true)
        coEvery { syncFavoritesOnLoginUseCase(any()) } returns Unit
        coEvery { syncCartOnLoginUseCase(any()) } returns Unit

        viewModel.onAction(AuthAction.SubmitClicked)
        
        advanceUntilIdle()

        assertEquals(AuthUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun `submit in login mode failure shows error`() = runTest(testDispatcher) {
        val email = "test@example.com"
        val password = "password123"
        viewModel.onAction(AuthAction.EmailChanged(email))
        viewModel.onAction(AuthAction.PasswordChanged(password))

        val errorMessage = "Invalid credentials"
        coEvery { loginUseCase(email, password) } returns Result.failure(Exception(errorMessage))

        viewModel.onAction(AuthAction.SubmitClicked)
        
        advanceUntilIdle()

        val state = viewModel.uiState.value as AuthUiState.Form
        assertEquals(errorMessage, state.emailError)
    }

    @Test
    fun `submit in register mode with password mismatch shows error`() = runTest(testDispatcher) {
        viewModel.onAction(AuthAction.ToggleMode)
        viewModel.onAction(AuthAction.FirstNameChanged("Ada"))
        viewModel.onAction(AuthAction.LastNameChanged("Lovelace"))
        viewModel.onAction(AuthAction.EmailChanged("ada@example.com"))
        viewModel.onAction(AuthAction.PasswordChanged("password123"))
        viewModel.onAction(AuthAction.ConfirmPasswordChanged("mismatch"))

        every { context.getString(any()) } returns "Passwords do not match"

        viewModel.onAction(AuthAction.SubmitClicked)

        val state = viewModel.uiState.value as AuthUiState.Form
        assertEquals("Passwords do not match", state.confirmPasswordError)
        coVerify(exactly = 0) { registerUseCase(any(), any(), any(), any()) }
    }

    @Test
    fun `submit in login mode emits Loading then Success on success`() = runTest(testDispatcher) {
        val email = "test@example.com"
        val password = "password123"
        viewModel.onAction(AuthAction.EmailChanged(email))
        viewModel.onAction(AuthAction.PasswordChanged(password))

        val mockUser = mockk<FirebaseUser>(relaxed = true) {
            every { isEmailVerified } returns true
            every { reload() } returns Tasks.forResult(null)
            every { uid } returns "user_id"
        }
        every { firebaseAuth.currentUser } returns mockUser
        
        coEvery { loginUseCase(email, password) } coAnswers {
            delay(1000)
            Result.success(mockk(relaxed = true))
        }
        coEvery { getShopifyTokenUseCase() } returns mockk(relaxed = true)
        coEvery { syncFavoritesOnLoginUseCase(any()) } returns Unit
        coEvery { syncCartOnLoginUseCase(any()) } returns Unit

        viewModel.uiState.test {
            // Initial state (emitted upon collection start)
            assertTrue(awaitItem() is AuthUiState.Form)
            
            viewModel.onAction(AuthAction.SubmitClicked)
            
            // Should emit Loading
            assertEquals(AuthUiState.Loading, awaitItem())
            
            // Should eventually emit Success
            assertEquals(AuthUiState.Success, awaitItem())
        }
    }

    @Test
    fun `backToLoginClicked resets state to login form and signs out`() {
        // Move to register
        viewModel.onAction(AuthAction.ToggleMode)
        assertFalse((viewModel.uiState.value as AuthUiState.Form).isLoginMode)

        viewModel.onAction(AuthAction.BackToLoginClicked)

        assertTrue((viewModel.uiState.value as AuthUiState.Form).isLoginMode)
        verify { firebaseAuth.signOut() }
    }

    @Test
    fun `triggerGoogleSignIn sends TriggerGoogleSignIn event`() = runTest(testDispatcher) {
        viewModel.events.test {
            viewModel.onAction(AuthAction.GoogleClicked)
            assertEquals(AuthEvent.TriggerGoogleSignIn, awaitItem())
        }
    }

    @Test
    fun `loginWithGoogle success transitions to Success`() = runTest(testDispatcher) {
        val idToken = "id_token"
        coEvery { loginWithGoogleUseCase(idToken) } returns Result.success(mockk(relaxed = true))
        coEvery { syncFavoritesOnLoginUseCase(any()) } returns Unit
        coEvery { syncCartOnLoginUseCase(any()) } returns Unit
        
        val mockUser = mockk<FirebaseUser>(relaxed = true) {
            every { uid } returns "user_id"
        }
        every { firebaseAuth.currentUser } returns mockUser

        viewModel.onAction(AuthAction.GoogleIdTokenReceived(idToken))
        
        advanceUntilIdle()

        assertEquals(AuthUiState.Success, viewModel.uiState.value)
        coVerify { syncFavoritesOnLoginUseCase("user_id") }
        coVerify { syncCartOnLoginUseCase("user_id") }
    }

    @Test
    fun `loginWithGoogle failure shows error`() = runTest(testDispatcher) {
        val idToken = "id_token"
        val errorMessage = "Google sign-in failed"
        coEvery { loginWithGoogleUseCase(idToken) } returns Result.failure(Exception(errorMessage))

        viewModel.onAction(AuthAction.GoogleIdTokenReceived(idToken))
        advanceUntilIdle()

        val state = viewModel.uiState.value as AuthUiState.Form
        assertFalse(state.isGoogleLoading)
        assertEquals(errorMessage, state.emailError)
    }

    @Test
    fun `onGoogleFailure updates state with error`() = runTest(testDispatcher) {
        val message = "Google Sign-In Cancelled"
        viewModel.onAction(AuthAction.GoogleSignInFailed(message))

        val state = viewModel.uiState.value as AuthUiState.Form
        assertFalse(state.isGoogleLoading)
        assertEquals(message, state.emailError)
    }

    @Test
    fun `post-auth sync failures do not prevent Success state`() = runTest(testDispatcher) {
        val email = "test@example.com"
        val password = "password123"
        viewModel.onAction(AuthAction.EmailChanged(email))
        viewModel.onAction(AuthAction.PasswordChanged(password))

        val mockUser = mockk<FirebaseUser>(relaxed = true) {
            every { isEmailVerified } returns true
            every { reload() } returns Tasks.forResult(null)
            every { uid } returns "user_id"
        }
        every { firebaseAuth.currentUser } returns mockUser
        
        coEvery { loginUseCase(email, password) } returns Result.success(mockk(relaxed = true))
        coEvery { getShopifyTokenUseCase() } returns mockk(relaxed = true)
        
        // Sync failures
        coEvery { syncFavoritesOnLoginUseCase(any()) } throws Exception("Sync favorites failed")
        coEvery { syncCartOnLoginUseCase(any()) } throws Exception("Sync cart failed")

        viewModel.onAction(AuthAction.SubmitClicked)
        advanceUntilIdle()

        assertEquals(AuthUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun `handlePostEmailAuthSuccess resets to Form if user is null`() = runTest(testDispatcher) {
        coEvery { loginUseCase(any(), any()) } returns Result.success(mockk(relaxed = true))
        every { firebaseAuth.currentUser } returns null // User disappears

        viewModel.onAction(AuthAction.SubmitClicked)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is AuthUiState.Form)
    }

    @Test
    fun `handlePostEmailAuthSuccess new registration sends verification email`() = runTest(testDispatcher) {
        viewModel.onAction(AuthAction.ToggleMode) // Register mode
        viewModel.onAction(AuthAction.FirstNameChanged("John"))
        viewModel.onAction(AuthAction.LastNameChanged("Doe"))
        viewModel.onAction(AuthAction.EmailChanged("john@example.com"))
        viewModel.onAction(AuthAction.PasswordChanged("password"))
        viewModel.onAction(AuthAction.ConfirmPasswordChanged("password"))

        val mockUser = mockk<FirebaseUser>(relaxed = true) {
            every { isEmailVerified } returns false
            every { email } returns "john@example.com"
            every { reload() } returns Tasks.forResult(null)
            every { sendEmailVerification() } returns Tasks.forResult(null)
        }
        every { firebaseAuth.currentUser } returns mockUser
        coEvery { registerUseCase(any(), any(), any(), any()) } returns Result.success(mockk(relaxed = true))

        viewModel.onAction(AuthAction.SubmitClicked)
        advanceUntilIdle()

        val state = viewModel.uiState.value as AuthUiState.EmailVerificationPending
        assertEquals("john@example.com", state.email)
        verify { mockUser.sendEmailVerification() }
    }

    @Test
    fun `handlePostEmailAuthSuccess login for unverified user does not send verification email`() = runTest(testDispatcher) {
        val email = "unverified@example.com"
        viewModel.onAction(AuthAction.EmailChanged(email))
        viewModel.onAction(AuthAction.PasswordChanged("password"))

        val mockUser = mockk<FirebaseUser>(relaxed = true)
        every { mockUser.isEmailVerified } returns false
        every { mockUser.email } returns email
        every { mockUser.reload() } returns Tasks.forResult(null)
        every { firebaseAuth.currentUser } returns mockUser
        coEvery { loginUseCase(any(), any()) } returns Result.success(mockk(relaxed = true))

        viewModel.onAction(AuthAction.SubmitClicked)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected EmailVerificationPending but was $state", state is AuthUiState.EmailVerificationPending)
        verify(exactly = 0) { mockUser.sendEmailVerification() }
    }

    @Test
    fun `resendVerificationEmail respects cooldown`() = runTest(testDispatcher) {
        val email = "test@example.com"
        viewModel.onAction(AuthAction.ToggleMode)
        viewModel.onAction(AuthAction.FirstNameChanged("John"))
        viewModel.onAction(AuthAction.LastNameChanged("Doe"))
        viewModel.onAction(AuthAction.EmailChanged(email))
        viewModel.onAction(AuthAction.PasswordChanged("password"))
        viewModel.onAction(AuthAction.ConfirmPasswordChanged("password"))

        val mockUser = mockk<FirebaseUser>(relaxed = true)
        every { mockUser.isEmailVerified } returns false
        every { mockUser.email } returns email
        every { mockUser.reload() } returns Tasks.forResult(null)
        every { mockUser.sendEmailVerification() } returns Tasks.forResult(null)
        every { firebaseAuth.currentUser } returns mockUser
        coEvery { registerUseCase(any(), any(), any(), any()) } returns Result.success(mockk(relaxed = true))

        // Trigger verification pending with cooldown
        viewModel.onAction(AuthAction.SubmitClicked)
        runCurrent()
        
        val state = viewModel.uiState.value
        assertTrue("Expected EmailVerificationPending but was $state", state is AuthUiState.EmailVerificationPending)
        state as AuthUiState.EmailVerificationPending
        assertTrue(state.resendCooldownSeconds > 0)
        
        clearMocks(mockUser, answers = false)
        
        // Try resend during cooldown
        viewModel.onAction(AuthAction.ResendVerificationClicked)
        
        verify(exactly = 0) { mockUser.sendEmailVerification() }
    }

    @Test
    fun `checkEmailVerified success transitions to Success`() = runTest(testDispatcher) {
        val email = "unverified@example.com"
        viewModel.onAction(AuthAction.EmailChanged(email))
        viewModel.onAction(AuthAction.PasswordChanged("password"))

        val mockUser = mockk<FirebaseUser>(relaxed = true)
        every { mockUser.isEmailVerified } returns false
        every { mockUser.email } returns email
        every { mockUser.reload() } returns Tasks.forResult(null)
        every { mockUser.uid } returns "user_id"
        every { firebaseAuth.currentUser } returns mockUser
        
        // Set state to pending
        coEvery { loginUseCase(any(), any()) } returns Result.success(mockk(relaxed = true))
        viewModel.onAction(AuthAction.SubmitClicked)
        advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value is AuthUiState.EmailVerificationPending)

        // Now mock as verified
        every { mockUser.isEmailVerified } returns true
        coEvery { getShopifyTokenUseCase() } returns mockk(relaxed = true)
        coEvery { syncFavoritesOnLoginUseCase(any()) } returns Unit
        coEvery { syncCartOnLoginUseCase(any()) } returns Unit
        
        viewModel.onAction(AuthAction.CheckVerificationClicked)
        advanceUntilIdle()
        
        assertEquals(AuthUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun `forgot password flow`() = runTest(testDispatcher) {
        val email = "reset@example.com"
        viewModel.onAction(AuthAction.EmailChanged(email))
        
        viewModel.onAction(AuthAction.ForgotPasswordClicked)
        
        val state = viewModel.uiState.value as AuthUiState.ForgotPassword
        assertEquals(email, state.email)
        
        val newEmail = "new@example.com"
        viewModel.onAction(AuthAction.ForgotPasswordEmailChanged(newEmail))
        assertEquals(newEmail, (viewModel.uiState.value as AuthUiState.ForgotPassword).email)
        
        every { firebaseAuth.sendPasswordResetEmail(newEmail) } returns Tasks.forResult(null)
        
        viewModel.onAction(AuthAction.SendResetLinkClicked)
        advanceUntilIdle()
        
        assertTrue((viewModel.uiState.value as AuthUiState.ForgotPassword).isEmailSent)
        
        viewModel.onAction(AuthAction.BackToLoginFromForgotPasswordClicked)
        val finalState = viewModel.uiState.value as AuthUiState.Form
        assertTrue(finalState.isLoginMode)
        assertEquals(newEmail, finalState.email)
    }

    @Test
    fun `isOnline flow reflects network monitor status`() = runTest(testDispatcher) {
        val networkFlow = MutableSharedFlow<Boolean>()
        every { networkMonitor.isOnline } returns networkFlow
        
        // Re-create VM to use the new flow
        val vm = AuthViewModel(
            context, loginUseCase, registerUseCase, loginWithGoogleUseCase,
            getShopifyTokenUseCase, syncFavoritesOnLoginUseCase, syncCartOnLoginUseCase, networkMonitor
        )
        
        vm.isOnline.test {
            // Initial value (since we use stateIn, it might emit initialValue before the flow)
            // But stateIn uses the first value from the flow if it's available.
            
            networkFlow.emit(true)
            assertTrue(awaitItem())
            
            networkFlow.emit(false)
            assertFalse(awaitItem())
            
            networkFlow.emit(true)
            assertTrue(awaitItem())
        }
    }

    @Test
    fun `field and toggle actions`() {
        viewModel.onAction(AuthAction.FirstNameChanged("John"))
        viewModel.onAction(AuthAction.LastNameChanged("Doe"))
        viewModel.onAction(AuthAction.ConfirmPasswordChanged("pass"))
        
        val state = viewModel.uiState.value as AuthUiState.Form
        assertEquals("John", state.firstName)
        assertEquals("Doe", state.lastName)
        assertEquals("pass", state.confirmPassword)
        
        // Toggles
        assertFalse(state.isPasswordVisible)
        viewModel.onAction(AuthAction.TogglePasswordVisibility)
        assertTrue((viewModel.uiState.value as AuthUiState.Form).isPasswordVisible)
        viewModel.onAction(AuthAction.TogglePasswordVisibility)
        assertFalse((viewModel.uiState.value as AuthUiState.Form).isPasswordVisible)
        
        assertFalse(state.isConfirmPasswordVisible)
        viewModel.onAction(AuthAction.ToggleConfirmPasswordVisibility)
        assertTrue((viewModel.uiState.value as AuthUiState.Form).isConfirmPasswordVisible)
    }
}
