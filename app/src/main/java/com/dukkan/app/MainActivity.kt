package com.dukkan.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dukkan.design_system.theme.AppTheme
import com.dukkan.domain.model.ThemeMode
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = hiltViewModel()
            val themeMode by appViewModel.themeMode.collectAsState()
            val language by appViewModel.language.collectAsState()
            val startDestination by appViewModel.startDestination.collectAsState()

            if (themeMode == null || language == null || startDestination == null) {
                // Wait for DataStore to load
                return@setContent
            }

            val darkTheme = when (themeMode!!) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            LaunchedEffect(language) {
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(language!!.tag)
                )
            }

            AppTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val showBottomBar = isTopLevelDestination(currentBackStackEntry?.destination)

                Box(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(
                        navController = navController,
                        startDestination = startDestination!!,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (showBottomBar) {
                        DukkanBottomBar(
                            navController = navController,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }
    }
}
