package com.dukkan.address.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dukkan.address.R
import com.dukkan.design_system.components.PrimaryButton
import com.dukkan.domain.model.Address

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressFormSheet(
    initialAddress: Address?,
    isSaving: Boolean,
    formError: String?,
    onDismissRequest: () -> Unit,
    onSave: (Address) -> Unit,
) {
    var firstName by remember(initialAddress) { mutableStateOf(initialAddress?.firstName.orEmpty()) }
    var lastName by remember(initialAddress) { mutableStateOf(initialAddress?.lastName.orEmpty()) }
    var company by remember(initialAddress) { mutableStateOf(initialAddress?.company.orEmpty()) }
    var address1 by remember(initialAddress) { mutableStateOf(initialAddress?.address1.orEmpty()) }
    var address2 by remember(initialAddress) { mutableStateOf(initialAddress?.address2.orEmpty()) }
    var city by remember(initialAddress) { mutableStateOf(initialAddress?.city.orEmpty()) }
    var province by remember(initialAddress) { mutableStateOf(initialAddress?.province.orEmpty()) }
    var country by remember(initialAddress) { mutableStateOf(initialAddress?.country.orEmpty()) }
    var zip by remember(initialAddress) { mutableStateOf(initialAddress?.zip.orEmpty()) }
    var phone by remember(initialAddress) { mutableStateOf(initialAddress?.phone.orEmpty()) }

    val isPhoneValid = isValidPhoneNumber(phone)
    val isValid =
        address1.isNotBlank() && city.isNotBlank() && country.isNotBlank() && phone.isNotBlank() && isPhoneValid

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = if (initialAddress == null) {
                    stringResource(R.string.saved_addresses_form_title_add)
                } else {
                    stringResource(R.string.saved_addresses_form_title_edit)
                },
                style = MaterialTheme.typography.titleLarge,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AddressTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    placeholder = stringResource(R.string.saved_addresses_field_first_name),
                    imeAction = ImeAction.Next,
                    modifier = Modifier.weight(1f),
                )
                AddressTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    placeholder = stringResource(R.string.saved_addresses_field_last_name),
                    imeAction = ImeAction.Next,
                    modifier = Modifier.weight(1f),
                )
            }
            AddressTextField(
                value = company,
                onValueChange = { company = it },
                placeholder = stringResource(R.string.saved_addresses_field_company),
                imeAction = ImeAction.Next,
            )
            AddressTextField(
                value = address1,
                onValueChange = { address1 = it },
                placeholder = stringResource(R.string.saved_addresses_field_address1),
                imeAction = ImeAction.Next,
            )
            AddressTextField(
                value = address2,
                onValueChange = { address2 = it },
                placeholder = stringResource(R.string.saved_addresses_field_address2),
                imeAction = ImeAction.Next,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AddressTextField(
                    value = city,
                    onValueChange = { city = it },
                    placeholder = stringResource(R.string.saved_addresses_field_city),
                    imeAction = ImeAction.Next,
                    modifier = Modifier.weight(1f),
                )
                AddressTextField(
                    value = province,
                    onValueChange = { province = it },
                    placeholder = stringResource(R.string.saved_addresses_field_province),
                    imeAction = ImeAction.Next,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CountryDropdownField(
                    value = country,
                    onValueChange = { country = it },
                    placeholder = stringResource(R.string.saved_addresses_field_country),
                    modifier = Modifier.weight(1f),
                )
                AddressTextField(
                    value = zip,
                    onValueChange = { zip = it },
                    placeholder = stringResource(R.string.saved_addresses_field_zip),
                    imeAction = ImeAction.Next,
                    modifier = Modifier.weight(1f),
                )
            }
            AddressTextField(
                value = phone,
                onValueChange = { phone = it },
                placeholder = stringResource(R.string.saved_addresses_field_phone),
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions.Default,
                errorMessage = if (phone.isNotBlank() && !isPhoneValid) {
                    stringResource(R.string.saved_addresses_error_invalid_phone)
                } else {
                    null
                },
            )

            if (formError != null) {
                Text(
                    text = formError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                PrimaryButton(
                    text = stringResource(R.string.saved_addresses_save),
                    onClick = {
                        onSave(
                            Address(
                                id = initialAddress?.id,
                                firstName = firstName.trim().ifBlank { null },
                                lastName = lastName.trim().ifBlank { null },
                                company = company.trim().ifBlank { null },
                                address1 = address1.trim().ifBlank { null },
                                address2 = address2.trim().ifBlank { null },
                                city = city.trim().ifBlank { null },
                                province = province.trim().ifBlank { null },
                                country = country.trim().ifBlank { null },
                                zip = zip.trim().ifBlank { null },
                                phone = phone.trim().ifBlank { null },
                                isDefault = initialAddress?.isDefault ?: false,
                            )
                        )
                    },
                    enabled = isValid,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private val phoneRegex = Regex("^[+]?[0-9\\s-]{7,20}$")

private fun isValidPhoneNumber(phone: String): Boolean {
    val trimmed = phone.trim()
    if (!phoneRegex.matches(trimmed)) return false
    val digitCount = trimmed.count { it.isDigit() }
    return digitCount in 7..15
}
