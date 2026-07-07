package com.dukkan.app

import android.app.Application
import android.content.pm.PackageManager
import dagger.hilt.android.HiltAndroidApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

@HiltAndroidApp
class DukkanApp : Application() {

    override fun onCreate() {
        super.onCreate()
        setupPaymob()
    }

    fun setupPaymob() {
        // Plant Timber tree so Paymob SDK logging works
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialise Koin — required by Paymob SDK which uses Koin internally
        startKoin {
            androidContext(this@DukkanApp)
        }
    }
}