package com.dukkan.design_system.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = DukkanAccent,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,
    onBackground = LightText,
    onSurface = LightText,
    onSurfaceVariant = LightMutedText,
    outline = LightBorder,
    inverseOnSurface = LightSubText
)

private val DarkColorScheme = darkColorScheme(
    primary = DukkanAccentDark,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onBackground = DarkText,
    onSurface = DarkText,
    onSurfaceVariant = DarkMutedText,
    outline = DarkBorder,
    inverseOnSurface = DarkSubText
)

data class DukkanExtendedColors(
    val bannerTitle: Color,
    val orderStatusInTransit: Color,
    val orderStatusProcessing: Color,
)

val LocalExtendedColors = staticCompositionLocalOf {
    DukkanExtendedColors(
        bannerTitle = Color.Unspecified,
        orderStatusInTransit = Color.Unspecified,
        orderStatusProcessing = Color.Unspecified,
    )
}
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val extendedColors = if (darkTheme) {
        DukkanExtendedColors(
            bannerTitle = DukkanBannerTitle,
            orderStatusInTransit = DarkOrderInTransit,
            orderStatusProcessing = DarkOrderProcessing,
        )
    } else {
        DukkanExtendedColors(
            bannerTitle = DukkanBannerTitle,
            orderStatusInTransit = LightOrderInTransit,
            orderStatusProcessing = LightOrderProcessing,
        )
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = DukkanTypography,
            content = content
        )
    }
}

// A handy object to make accessing it look just like MaterialTheme
object DukkanTheme {
    val extendedColors: DukkanExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}