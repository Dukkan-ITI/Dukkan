package com.dukkan.payment.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dukkan.domain.model.Address
import com.dukkan.payment.R
import com.dukkan.payment.domain.model.CheckoutAddress
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable

@Composable
internal fun AddressSection(
    selectedAddress: CheckoutAddress?,
    addresses: List<Address>,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 22.dp)) {
        SectionEyebrow(text = stringResource(R.string.payment_step_address_title))

        Spacer(modifier = Modifier.height(10.dp))

        val actualAddress = when (selectedAddress) {
            is CheckoutAddress.Saved -> addresses.find { it.id == selectedAddress.addressId }
            is CheckoutAddress.OneOff -> selectedAddress.address
            null -> null
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (actualAddress != null) {
                        Text(
                            text = listOfNotNull(actualAddress.firstName, actualAddress.lastName)
                                .joinToString(" ")
                                .ifBlank { stringResource(R.string.payment_address_unknown) },
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = listOfNotNull(
                                listOfNotNull(actualAddress.address1, actualAddress.city)
                                    .filter { it.isNotBlank() }
                                    .joinToString(", ")
                                    .ifBlank { null },
                                actualAddress.phone,
                            ).joinToString("\n"),
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.payment_address_none_selected),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Start,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(1.dp))

                Text(
                    text = stringResource(R.string.payment_address_edit),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onEditClick),
                )
            }
        }
    }
}

@Composable
internal fun SectionEyebrow(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.outline,
        modifier = modifier,
    )
}