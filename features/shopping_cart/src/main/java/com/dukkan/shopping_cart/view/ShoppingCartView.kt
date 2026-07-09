package com.dukkan.shopping_cart.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dukkan.design_system.components.ChatFab
import com.dukkan.design_system.components.AuthRequiredPlaceholder
import com.dukkan.domain.model.cart.CartLine
import com.dukkan.shopping_cart.R
import com.dukkan.shopping_cart.components.CartItemRow
import com.dukkan.shopping_cart.components.CartItemRowShimmer
import com.dukkan.shopping_cart.components.EmptyCartState
import com.dukkan.shopping_cart.components.PromoCodeSection
import com.dukkan.shopping_cart.components.RemoveItemDialog
import com.dukkan.shopping_cart.components.SummarySection
import com.dukkan.shopping_cart.uistate.ShoppingCartState
import com.dukkan.shopping_cart.viewmodel.ShoppingCartViewModel
import androidx.compose.material3.Scaffold
import com.dukkan.design_system.components.bottomBarSpace
import com.dukkan.design_system.R as DesignSystemR

@Composable
fun ShoppingCartView(
    onSignInClick: () -> Unit,
    onNavigateToChat: () -> Unit = {},
    viewModel: ShoppingCartViewModel = hiltViewModel(),
    onStartShoppingClick: () -> Unit = {},
    onCheckoutClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onScreenEntered()
    }

    if (!isLoggedIn) {
        AuthRequiredPlaceholder(
            title = stringResource(id = R.string.cart_title),
            onSignInClick = onSignInClick
        )
        return
    }

    ShoppingCartContent(
        state = state,
        onStartShoppingClick = onStartShoppingClick,
        onCheckoutClick = onCheckoutClick,
        onNavigateToChat = onNavigateToChat,
        onQuantityChanged = viewModel::updateQuantity,
        onRemoveClick = { viewModel.showRemoveDialog(it) },
        onPromoCodeChange = viewModel::onPromoCodeChange,
        onApplyPromoCode = viewModel::applyPromoCode,
        onRemovePromoCode = viewModel::removePromoCode,
        onDismissRemoveDialog = viewModel::dismissRemoveDialog,
        onConfirmRemoveItem = viewModel::confirmRemoveItem
    )
}

@Composable
private fun ShoppingCartContent(
    state: ShoppingCartState,
    onStartShoppingClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    onNavigateToChat: () -> Unit,
    onQuantityChanged: (CartLine, Int) -> Unit,
    onRemoveClick: (CartLine) -> Unit,
    onPromoCodeChange: (String) -> Unit,
    onApplyPromoCode: () -> Unit,
    onRemovePromoCode: (String) -> Unit,
    onDismissRemoveDialog: () -> Unit,
    onConfirmRemoveItem: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ChatFab(onClick = onNavigateToChat,
                modifier = Modifier.padding(bottom = bottomBarSpace()))
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.Bottom) {
                        Text(
                            text = stringResource(R.string.your_bag),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(
                                R.string.items_count_format,
                                state.cart?.lines?.size ?: 0
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                if (!state.isOnline) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(vertical = 6.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            text = stringResource(com.dukkan.design_system.R.string.viewing_cached_data),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (state.isLoading) {
                        items(3) {
                            CartItemRowShimmer()
                        }
                    } else if (state.cart?.lines.isNullOrEmpty()) {
                        item {
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)) {
                                EmptyCartState(onStartShoppingClick)
                            }
                        }
                    } else {
                        items(state.cart?.lines ?: emptyList(), key = { it.id }) { item ->
                            CartItemRow(
                                item = item,
                                onQuantityChanged = onQuantityChanged,
                                onRemoveClick = { onRemoveClick(item) }
                            )
                        }

                        item {
                            PromoCodeSection(
                                promoCode = state.promoCode,
                                appliedCodes = state.cart?.appliedDiscounts?.map { it.code }
                                    ?: emptyList(),
                                onPromoCodeChange = onPromoCodeChange,
                                onApplyPromoCode = onApplyPromoCode,
                                onRemovePromoCode = onRemovePromoCode,
                                isApplying = state.isApplyingPromo,
                                error = state.promoError
                            )
                        }

                        item {
                            SummarySection(state)
                        }

                        item {
                            Button(
                                onClick = onCheckoutClick,
                                enabled = state.isOnline,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .padding(top = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.checkout_format,
                                        state.cart?.cost?.totalAmount?.asString()
                                            ?: stringResource(R.string.default_amount),
                                    ),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier
                            .navigationBarsPadding()
                            .height(80.dp))
                    }
                }
            }

            state.showRemoveDialogForItem?.let { item ->
                RemoveItemDialog(
                    item = item,
                    onDismiss = onDismissRemoveDialog,
                    onConfirm = onConfirmRemoveItem
                )
            }

            // Offline toast
            AnimatedVisibility(
                visible = state.showOfflineToast,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp, start = 24.dp, end = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .wrapContentWidth()
                        .background(MaterialTheme.colorScheme.error, RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(DesignSystemR.string.offline_title),
                        color = MaterialTheme.colorScheme.onError,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
