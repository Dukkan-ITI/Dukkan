package com.dukkan.shopping_cart.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dukkan.design_system.components.GuestPlaceholderScreen
import com.dukkan.design_system.components.bottomBarSpace
import com.dukkan.domain.model.CartItem
import com.dukkan.shopping_cart.R
import com.dukkan.shopping_cart.components.CartItemRow
import com.dukkan.shopping_cart.components.EmptyCartState
import com.dukkan.shopping_cart.components.PromoCodeSection
import com.dukkan.shopping_cart.components.RemoveItemDialog
import com.dukkan.shopping_cart.components.SummarySection
import com.dukkan.shopping_cart.uistate.ShoppingCartState
import com.dukkan.shopping_cart.viewmodel.ShoppingCartViewModel

@Composable
fun ShoppingCartScreen(
    onSignInClick: () -> Unit,
    viewModel: ShoppingCartViewModel = hiltViewModel(),
    onStartShoppingClick: () -> Unit = {},
    onCheckoutClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

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
        onDismissRemoveDialog = viewModel::dismissRemoveDialog,
        onConfirmRemoveItem = viewModel::confirmRemoveItem
    )
}

@Composable
private fun ShoppingCartContent(
    state: ShoppingCartState,
    onStartShoppingClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    onQuantityChanged: (CartItem, Int) -> Unit,
    onRemoveClick: (CartItem) -> Unit,
    onPromoCodeChange: (String) -> Unit,
    onApplyPromoCode: () -> Unit,
    onDismissRemoveDialog: () -> Unit,
    onConfirmRemoveItem: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F1A))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.Bottom) {
                    Text(
                        text = stringResource(R.string.your_bag),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.items_count_format, state.cartItems.size),
                        color = Color.Gray,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = bottomBarSpace())
            ) {
                if (state.cartItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {
                            EmptyCartState(onStartShoppingClick)
                        }
                    }
                } else {
                    items(state.cartItems, key = { it.id }) { item ->
                        CartItemRow(
                            item = item,
                            onQuantityChanged = onQuantityChanged,
                            onRemoveClick = { onRemoveClick(item) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    PromoCodeSection(
                        promoCode = state.promoCode,
                        onPromoCodeChange = onPromoCodeChange,
                        onApplyPromoCode = onApplyPromoCode,
                        isApplying = state.isApplyingPromo,
                        error = state.promoError
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SummarySection(state)

                    Button(
                        onClick = onCheckoutClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B8AFF))
                    ) {
                        Text(
                            text = stringResource(R.string.checkout_format, state.total),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
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






