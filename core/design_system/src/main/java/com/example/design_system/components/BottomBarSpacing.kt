package com.example.design_system.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Visual height of the floating bottom-nav pill. Shared by the bar and content spacing. */
val FloatingBottomBarHeight: Dp = 64.dp

/** Gap between the pill and the screen's bottom edge. */
val FloatingBottomBarMargin: Dp = 16.dp

/**
 * Bottom space a scrollable screen must leave so its last content clears the floating
 * nav bar: pill height + margin + the system navigation-bar inset (+ optional breathing gap).
 *
 * Spread into a Lazy list's `contentPadding`, e.g. `PaddingValues(bottom = bottomBarSpace())`.
 */
@Composable
fun bottomBarSpace(extra: Dp = 8.dp): Dp {
    val systemBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return FloatingBottomBarHeight + FloatingBottomBarMargin + systemBottom + extra
}

/** Drop-in Spacer for the bottom of a non-lazy Column, or as a list's trailing item. */
@Composable
fun BottomBarSpacer() = Spacer(Modifier.height(bottomBarSpace()))
