package com.dukkan.shopping_cart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dukkan.shopping_cart.uistate.ShoppingCartState
import com.dukkan.shopping_cart.R

@Composable
fun SummarySection(state: ShoppingCartState) {
    val subtotalMoney = state.cart?.cost?.subtotalAmount
    val totalMoney    = state.cart?.cost?.totalAmount
    val currency      = subtotalMoney?.currencyCode ?: ""

    val subtotalVal = subtotalMoney?.amount?.toDouble() ?: 0.0
    val totalVal    = totalMoney?.amount?.toDouble()    ?: 0.0
    val discountVal = subtotalVal - totalVal

    val subtotalRaw = subtotalMoney?.amount?.toPlainString() ?: "0.00"
    val totalRaw    = totalMoney?.amount?.toPlainString()    ?: "0.00"

    val discountCodes = state.cart?.appliedDiscounts ?: emptyList()
    val hasDiscount   = discountCodes.isNotEmpty() && discountVal > 0.001
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E2A), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.subtotal), color = Color.Gray, fontSize = 16.sp)
            Text("$subtotalRaw $currency", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Shipping", color = Color.Gray, fontSize = 16.sp)
            Text("Free", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        if (hasDiscount) {
            Spacer(modifier = Modifier.height(12.dp))
            discountCodes.forEach { discount ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Discount (${discount.code})",
                        color = Color(0xFF4CAF50),
                        fontSize = 15.sp
                    )
                    Text(
                        text = "-${
                            String.format("%.2f", discountVal)
                        } $currency",
                        color = Color(0xFF4CAF50),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else if (discountCodes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            discountCodes.forEach { discount ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Discount (${discount.code})", color = Color(0xFF4CAF50), fontSize = 15.sp)
                    Text("Applied ✓", color = Color(0xFF4CAF50), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Divider(color = Color(0xFF2C2C3E), thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.total), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("$totalRaw $currency", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}