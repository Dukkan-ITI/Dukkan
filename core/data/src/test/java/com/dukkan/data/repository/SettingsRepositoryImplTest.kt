package com.dukkan.data.repository

import app.cash.turbine.test
import com.dukkan.data.source.local.SettingsStore
import com.dukkan.domain.model.AppCurrency
import com.dukkan.domain.model.ThemeMode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryImplTest {

    private val store: SettingsStore = mockk()
    private val themeModeFlow = MutableStateFlow<String?>(null)
    private val currencyFlow = MutableStateFlow<String?>(null)
    private val languageFlow = MutableStateFlow<String?>(null)
    private val onboardingFlow = MutableStateFlow(false)
    
    private lateinit var repository: SettingsRepositoryImpl

    @Before
    fun setUp() {
        every { store.themeMode } returns themeModeFlow
        every { store.currency } returns currencyFlow
        every { store.language } returns languageFlow
        every { store.isOnboardingCompleted } returns onboardingFlow
        
        repository = SettingsRepositoryImpl(store)
    }

    @Test
    fun `themeMode returns mapped enum or default SYSTEM`() = runTest {
        repository.themeMode.test {
            assertEquals(ThemeMode.SYSTEM, awaitItem())
            
            themeModeFlow.value = "DARK"
            runCurrent()
            assertEquals(ThemeMode.DARK, awaitItem())
            
            themeModeFlow.value = "INVALID"
            runCurrent()
            assertEquals(ThemeMode.SYSTEM, awaitItem())
        }
    }

    @Test
    fun `currency returns mapped enum or default USD`() = runTest {
        repository.currency.test {
            assertEquals(AppCurrency.USD, awaitItem())
            
            currencyFlow.value = "EGP"
            runCurrent()
            assertEquals(AppCurrency.EGP, awaitItem())
        }
    }

    @Test
    fun `setCurrency calls store with enum name`() = runTest {
        coEvery { store.setCurrency(any()) } returns Unit
        repository.setCurrency(AppCurrency.EGP)
        coVerify { store.setCurrency("EGP") }
    }
}
