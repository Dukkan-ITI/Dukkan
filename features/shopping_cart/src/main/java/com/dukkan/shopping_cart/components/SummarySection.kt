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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.subtotal), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
            Text(
                stringResource(R.string.amount_with_currency_format, subtotalRaw, currency),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.shipping), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
            Text(stringResource(R.string.free), color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                        text = stringResource(R.string.discount_format, discount.code),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 15.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.discount_amount_format,
                            String.format("%.2f", discountVal),
                            currency,
                        ),
                        color = MaterialTheme.colorScheme.primary,
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
                    Text(stringResource(R.string.discount_format, discount.code), color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
                    Text(stringResource(R.string.applied), color = MaterialTheme.colorScheme.primary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.total), color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.amount_with_currency_format, totalRaw, currency),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}