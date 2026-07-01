package com.dukkan.shopping_cart.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dukkan.shopping_cart.components.CartItemRow
import com.dukkan.shopping_cart.components.EmptyCartState
import com.dukkan.shopping_cart.components.PromoCodeSection
import com.dukkan.shopping_cart.components.RemoveItemDialog
import com.dukkan.shopping_cart.components.SummarySection
import com.dukkan.shopping_cart.uistate.ShoppingCartState
import com.dukkan.shopping_cart.viewmodel.ShoppingCartViewModel
import com.dukkan.shopping_cart.R
import com.msayeh.domain.model.cart.CartLine
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource

@Composable
fun ShoppingCartView(
    viewModel: ShoppingCartViewModel = hiltViewModel(),
    onStartShoppingClick: () -> Unit = {},
    onCheckoutClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    
    ShoppingCartContent(
        state = state,
        onStartShoppingClick = onStartShoppingClick,
        onCheckoutClick = onCheckoutClick,
        onQuantityChanged = viewModel::updateQuantity,
        onRemoveClick = { viewModel.showRemoveDialog(it) },
        onPromoCodeChange = viewModel::onPromoCodeChange,
        onApplyPromoCode = viewModel::applyPromoCode,
        onDismissRemoveDialog = viewModel::dismissRemoveDialog,
        onConfirmRemoveItem = viewModel::confirmRemoveItem,
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
    onDismissRemoveDialog: () -> Unit,
    onConfirmRemoveItem: () -> Unit,
) {
    val cartLines = state.cart?.lines ?: emptyList()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F1A))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
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
                        text = stringResource(R.string.items_count_format, cartLines.size),
                        color = Color.Gray,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFF6B8AFF))
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (cartLines.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                            EmptyCartState(onStartShoppingClick)
                        }
                    }
                } else {
                    items(cartLines, key = { it.id }) { item ->
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
                    
                    val totalAmount = state.cart?.cost?.totalAmount
                    val totalText = if (totalAmount != null)
                        "${totalAmount.amount.toPlainString()} ${totalAmount.currencyCode}" else ""

                    Button(
                        onClick = onCheckoutClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B8AFF))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Checkout",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (totalText.isNotBlank()) {
                                Text(
                                    text = totalText,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
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






