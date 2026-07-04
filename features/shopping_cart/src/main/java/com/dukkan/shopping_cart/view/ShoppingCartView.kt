package com.dukkan.shopping_cart.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dukkan.shopping_cart.components.CartItemRow
import com.dukkan.domain.model.cart.CartLine
import com.dukkan.domain.model.asString
import com.dukkan.shopping_cart.components.EmptyCartState
import com.dukkan.shopping_cart.components.PromoCodeSection
import com.dukkan.shopping_cart.components.RemoveItemDialog
import com.dukkan.shopping_cart.components.SummarySection
import com.dukkan.shopping_cart.uistate.ShoppingCartState
import com.dukkan.shopping_cart.viewmodel.ShoppingCartViewModel
import com.dukkan.shopping_cart.R

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.dukkan.design_system.components.GuestPlaceholderScreen

@Composable
fun ShoppingCartView(
    onSignInClick: () -> Unit,
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
        GuestPlaceholderScreen(
            title = stringResource(id = R.string.cart_title),
            onSignInClick = onSignInClick
        )
        return
    }

    ShoppingCartContent(
        state = state,
        onStartShoppingClick = onStartShoppingClick,
        onCheckoutClick = onCheckoutClick,
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
    onQuantityChanged: (CartLine, Int) -> Unit,
    onRemoveClick: (CartLine) -> Unit,
    onPromoCodeChange: (String) -> Unit,
    onApplyPromoCode: () -> Unit,
    onRemovePromoCode: (String) -> Unit,
    onDismissRemoveDialog: () -> Unit,
    onConfirmRemoveItem: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
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
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.items_count_format, state.cart?.lines?.size ?: 0),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 4.dp)
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
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else if (state.cart?.lines.isNullOrEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
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
                }

                item {
                    PromoCodeSection(
                        promoCode = state.promoCode,
                        appliedCodes = state.cart?.appliedDiscounts?.map { it.code } ?: emptyList(),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
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

                item {
                    Spacer(modifier = Modifier.navigationBarsPadding().height(80.dp))
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
    }
}
