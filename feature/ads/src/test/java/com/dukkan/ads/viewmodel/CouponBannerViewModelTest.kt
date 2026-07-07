package com.dukkan.ads.viewmodel

import com.dukkan.domain.model.AuthUser
import com.dukkan.domain.model.Coupon
import com.dukkan.domain.usecase.auth.GetCurrentUserUseCase
import com.dukkan.domain.usecase.coupon.CouponUseCases
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CouponBannerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    lateinit var couponUseCases: CouponUseCases

    @MockK
    lateinit var getCurrentUser: GetCurrentUserUseCase

    private lateinit var viewModel: CouponBannerViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        every { couponUseCases.getAvailableCoupons() } returns emptyList()
        coEvery { getCurrentUser.invoke() } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization loads coupons and checks login status`() = runTest(testDispatcher) {
        val coupons = listOf(Coupon("C1", "CODE1", "10%", "DESC"))
        every { couponUseCases.getAvailableCoupons() } returns coupons
        coEvery { getCurrentUser.invoke() } returns mockk<AuthUser>()

        viewModel = CouponBannerViewModel(couponUseCases, getCurrentUser)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(coupons, state.coupons)
        assertTrue(state.isLoggedIn)
    }

    @Test
    fun `onCouponClick for guest shows guest dialog`() = runTest(testDispatcher) {
        coEvery { getCurrentUser.invoke() } returns null
        viewModel = CouponBannerViewModel(couponUseCases, getCurrentUser)
        advanceUntilIdle()

        viewModel.onCouponClick(mockk())

        assertTrue(viewModel.state.value.showGuestDialog)
    }

    @Test
    fun `onCouponClick for logged in user saves coupon and shows success`() = runTest(testDispatcher) {
        coEvery { getCurrentUser.invoke() } returns mockk<AuthUser>()
        viewModel = CouponBannerViewModel(couponUseCases, getCurrentUser)
        
        // Wait for checkLoginStatus to complete
        advanceUntilIdle()
        
        assertTrue("isLoggedIn should be true", viewModel.state.value.isLoggedIn)

        val coupon = Coupon("ID1", "SAVE10", "10%", "")
        coEvery { couponUseCases.saveCoupon("SAVE10") } returns Unit

        viewModel.onCouponClick(coupon)
        
        // The success state is set in a new coroutine, let's advance
        runCurrent()
        
        assertTrue("savedSuccess should be true", viewModel.state.value.savedSuccess)
        coVerify { couponUseCases.saveCoupon("SAVE10") }

        advanceUntilIdle() // Wait for delay(2000)
        assertFalse("savedSuccess should be false after delay", viewModel.state.value.savedSuccess)
    }

    @Test
    fun `dismissGuestDialog updates state`() = runTest(testDispatcher) {
        viewModel = CouponBannerViewModel(couponUseCases, getCurrentUser)
        advanceUntilIdle()

        viewModel.onCouponClick(mockk())
        assertTrue(viewModel.state.value.showGuestDialog)

        viewModel.dismissGuestDialog()
        assertFalse(viewModel.state.value.showGuestDialog)
    }
}
