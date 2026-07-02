package com.dukkan.app

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.dukkan.navigation.Screen
import com.example.design_system.components.FloatingBottomBarHeight
import com.example.design_system.components.FloatingBottomBarMargin

/**
 * The top-level destinations reachable from the bottom navigation bar, in display order.
 */
private enum class TopLevelDestination(
    val route: Any,
    val routeName: String,
    @StringRes val label: Int,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unselectedIcon: Int,
) {
    HOME(
        route = Screen.Home,
        routeName = Screen.Home.serializer().descriptor.serialName,
        label = R.string.bottom_bar_home,
        selectedIcon = R.drawable.ic_home_filled,
        unselectedIcon = R.drawable.ic_home_outlined,
    ),
    SEARCH(
        route = Screen.Search,
        routeName = Screen.Search.serializer().descriptor.serialName,
        label = R.string.bottom_bar_search,
        selectedIcon = R.drawable.ic_search_filled,
        unselectedIcon = R.drawable.ic_search_outlined,
    ),
    FAVORITE(
        route = Screen.Favorite,
        routeName = Screen.Favorite.serializer().descriptor.serialName,
        label = R.string.bottom_bar_favorites,
        selectedIcon = R.drawable.ic_favorite_filled,
        unselectedIcon = R.drawable.ic_favorite_outlined,
    ),
    CART(
        route = Screen.ShoppingCart,
        routeName = Screen.ShoppingCart.serializer().descriptor.serialName,
        label = R.string.bottom_bar_cart,
        selectedIcon = R.drawable.ic_cart_filled,
        unselectedIcon = R.drawable.ic_cart_outlined,
    ),
    PROFILE(
        route = Screen.Profile,
        routeName = Screen.Profile.serializer().descriptor.serialName,
        label = R.string.bottom_bar_profile,
        selectedIcon = R.drawable.ic_profile_filled,
        unselectedIcon = R.drawable.ic_profile_outlined,
    ),
}

private fun NavDestination.matches(dest: TopLevelDestination): Boolean =
    hierarchy.any { it.route == dest.routeName }

fun isTopLevelDestination(destination: NavDestination?): Boolean =
    TopLevelDestination.entries.any { dest -> destination?.matches(dest) == true }

@Composable
fun DukkanBottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = FloatingBottomBarMargin),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(FloatingBottomBarHeight)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TopLevelDestination.entries.forEach { dest ->
                val selected = currentDestination?.matches(dest) == true
                DukkanBottomBarItem(
                    destination = dest,
                    selected = selected,
                    onClick = {
                        navController.navigate(dest.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun DukkanBottomBarItem(
    destination: TopLevelDestination,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(48.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = null,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(
                if (selected) destination.selectedIcon else destination.unselectedIcon
            ),
            contentDescription = stringResource(destination.label),
            tint = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(26.dp),
        )
    }
}
