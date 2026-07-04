package com.dukkan.payment.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import com.msayeh.domain.model.Address

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
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Edit Address for this Order",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = address1,
                onValueChange = { address1 = it },
                label = { Text("Street Address") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
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
                            // Preserve existing IDs or fields if needed, but since it's OneOff we don't need ID
                        )
                    )
                    onDismiss()
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Use this Address")
            }
        }
    }
}
