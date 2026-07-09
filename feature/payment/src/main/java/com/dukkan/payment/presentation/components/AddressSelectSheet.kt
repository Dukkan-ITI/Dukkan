package com.dukkan.payment.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dukkan.domain.model.Address
import com.dukkan.payment.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddressSelectSheet(
    addresses: List<Address>,
    selectedAddressId: String?,
    onSelect: (String) -> Unit,
    onAddNew: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.payment_select_address_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                addresses.forEach { address ->
                    val id = address.id
                    AddressCard(
                        address = address,
                        isSelected = id != null && id == selectedAddressId,
                        onSelect = { if (id != null) onSelect(id) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedButton(
                onClick = onAddNew,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.payment_add_new_address))
            }
        }
    }
}
