package com.dukkan.payment.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dukkan.domain.model.Address

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.dukkan.payment.R
import com.dukkan.payment.presentation.components.PaymentAddressTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddressEditSheet(
    currentAddress: Address?,
    onDismiss: () -> Unit,
    onSave: (Address) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var firstName by remember { mutableStateOf(currentAddress?.firstName ?: "") }
    var lastName by remember { mutableStateOf(currentAddress?.lastName ?: "") }
    var address1 by remember { mutableStateOf(currentAddress?.address1 ?: "") }
    var city by remember { mutableStateOf(currentAddress?.city ?: "") }
    var phone by remember { mutableStateOf(currentAddress?.phone ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = {
            androidx.compose.material3.BottomSheetDefaults.DragHandle()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.payment_address_delivery),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PaymentAddressTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    placeholder = stringResource(R.string.payment_address_first_name),
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Next
                )
                
                PaymentAddressTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    placeholder = stringResource(R.string.payment_address_last_name),
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Next
                )
            }
            
            PaymentAddressTextField(
                value = address1,
                onValueChange = { address1 = it },
                placeholder = stringResource(R.string.payment_address_street),
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Next
            )
            
            PaymentAddressTextField(
                value = city,
                onValueChange = { city = it },
                placeholder = stringResource(R.string.payment_address_city),
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Next
            )
            
            PaymentAddressTextField(
                value = phone,
                onValueChange = { phone = it },
                placeholder = stringResource(R.string.payment_address_phone),
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val isFormValid = firstName.isNotBlank() && lastName.isNotBlank() && 
                              address1.isNotBlank() && city.isNotBlank() && phone.isNotBlank()
            
            Button(
                onClick = {
                    onSave(
                        Address(
                            firstName = firstName,
                            lastName = lastName,
                            address1 = address1,
                            city = city,
                            phone = phone,
                        )
                    )
                    onDismiss()
                },
                enabled = isFormValid,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.payment_address_confirm),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
