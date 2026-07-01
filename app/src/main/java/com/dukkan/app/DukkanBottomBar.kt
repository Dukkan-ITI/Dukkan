package com.dukkan.app

import androidx.annotation.DrawableRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.dukkan.navigation.Screen
import kotlin.reflect.KClass

/**
 * The top-level destinations reachable from the bottom navigation bar, in display order.
 */
private enum class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    val label: String,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unselectedIcon: Int,
) {
    HOME(
        route = Screen.Home,
        routeClass = Screen.Home::class,
        label = "Home",
        selectedIcon = R.drawable.ic_home_filled,
        unselectedIcon = R.drawable.ic_home_outlined,
    ),
    SEARCH(
        route = Screen.Search,
        routeClass = Screen.Search::class,
        label = "Search",
        selectedIcon = R.drawable.ic_search_filled,
        unselectedIcon = R.drawable.ic_search_outlined,
    ),
    FAVORITE(
        route = Screen.Favorite,
        routeClass = Screen.Favorite::class,
        label = "Favorites",
        selectedIcon = R.drawable.ic_favorite_filled,
        unselectedIcon = R.drawable.ic_favorite_outlined,
    ),
    CART(
        route = Screen.ShoppingCart,
        routeClass = Screen.ShoppingCart::class,
        label = "Cart",
        selectedIcon = R.drawable.ic_cart_filled,
        unselectedIcon = R.drawable.ic_cart_outlined,
    ),
    PROFILE(
        route = Screen.Profile,
        routeClass = Screen.Profile::class,
        label = "Profile",
        selectedIcon = R.drawable.ic_profile_filled,
        unselectedIcon = R.drawable.ic_profile_outlined,
    ),
}

fun isTopLevelDestination(destination: NavDestination?): Boolean =
    TopLevelDestination.entries.any { dest ->
        destination?.hierarchy?.any { it.hasRoute(dest.routeClass) } == true
    }

@Composable
fun DukkanBottomBar(navController: NavHostController) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TopLevelDestination.entries.forEach { dest ->
                val selected =
                    currentDestination?.hierarchy?.any { it.hasRoute(dest.routeClass) } == true
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
            contentDescription = destination.label,
            tint = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(26.dp),
        )
    }
}
