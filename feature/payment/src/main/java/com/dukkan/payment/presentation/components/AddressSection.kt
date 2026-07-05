package com.dukkan.payment.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dukkan.domain.model.Address
import com.dukkan.payment.R
import com.dukkan.payment.domain.model.CheckoutAddress

@Composable
internal fun AddressSection(
    selectedAddress: CheckoutAddress?,
    addresses: List<Address>,
    onEditClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .animateContentSize(spring(stiffness = Spring.StiffnessLow))
    ) {
        Text(
            text = stringResource(R.string.payment_step_address_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (selectedAddress == null) {
                    Text("No address selected", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    val actualAddress = when (selectedAddress) {
                        is CheckoutAddress.Saved -> addresses.find { it.id == selectedAddress.addressId }
                        is CheckoutAddress.OneOff -> selectedAddress.address
                    }
                    
                    if (actualAddress != null) {
                        Text(
                            text = "${actualAddress.firstName ?: ""} ${actualAddress.lastName ?: ""}".trim(),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "${actualAddress.address1}, ${actualAddress.city}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        actualAddress.phone?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text("Address details unavailable")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = onEditClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Edit for this order")
                }
            }
        }
    }
}
