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
        
        com.google.firebase.FirebaseApp.initializeApp(this)
        val firebaseAppCheck = com.google.firebase.appcheck.FirebaseAppCheck.getInstance()
        
        if (BuildConfig.DEBUG) {
            firebaseAppCheck.installAppCheckProviderFactory(
                com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory.getInstance()
            )
        } else {
            firebaseAppCheck.installAppCheckProviderFactory(
                com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }
        
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