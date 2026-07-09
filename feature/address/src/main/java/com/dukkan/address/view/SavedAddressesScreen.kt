package com.dukkan.address.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dukkan.address.R
import com.dukkan.address.components.AddressCard
import com.dukkan.address.components.DeleteAddressDialog
import com.dukkan.address_ui.AddressFormSheet
import com.dukkan.address_ui.LocationPickerScreen
import com.dukkan.address.viewmodel.SavedAddressesState
import com.dukkan.address.viewmodel.SavedAddressesViewModel
import com.dukkan.design_system.components.ErrorScreen
import com.dukkan.design_system.components.GuestAuthDialog
import com.dukkan.design_system.components.LoadingScreen
import com.dukkan.design_system.components.PrimaryButton
import com.dukkan.design_system.theme.AppTheme
import com.dukkan.domain.model.Address

@Composable
fun SavedAddressesScreen(
    onBackClick: () -> Unit = {},
    onSignInClick: () -> Unit = onBackClick,
    viewModel: SavedAddressesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    if (!isLoggedIn) {
        GuestAuthDialog(
            onDismiss = { onBackClick() },
            onSignInClick = onSignInClick,
        )
        return
    }

    SavedAddressesContent(
        state = state,
        onBackClick = onBackClick,
        onAddClick = viewModel::onAddClick,
        onEditClick = viewModel::onEditClick,
        onDeleteClick = viewModel::onDeleteClick,
        onSetDefaultClick = viewModel::onSetDefaultClick,
        onRetry = viewModel::loadAddresses,
        onDismissFormSheet = viewModel::dismissFormSheet,
        onSaveAddress = viewModel::saveAddress,
        onDismissDeleteDialog = viewModel::dismissDeleteDialog,
        onConfirmDelete = viewModel::confirmDelete,
        onMapClick = viewModel::onMapClick,
        onDismissMap = viewModel::dismissMap,
        onLocationSelected = viewModel::onLocationSelected,
        onClearSelectedLatLng = viewModel::clearSelectedLatLng,
    )
}

@Composable
private fun SavedAddressesContent(
    state: SavedAddressesState,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (Address) -> Unit,
    onDeleteClick: (Address) -> Unit,
    onSetDefaultClick: (Address) -> Unit,
    onRetry: () -> Unit,
    onDismissFormSheet: () -> Unit,
    onSaveAddress: (Address) -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDelete: () -> Unit,
    onMapClick: () -> Unit,
    onDismissMap: () -> Unit,
    onLocationSelected: (com.google.android.gms.maps.model.LatLng) -> Unit,
    onClearSelectedLatLng: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isMapVisible) {
        LocationPickerScreen(
            onLocationSelected = onLocationSelected,
            onCancel = onDismissMap,
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.saved_addresses_back),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.saved_addresses_title),
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when {
                state.isLoading && state.addresses.isEmpty() -> LoadingScreen()
                state.error != null && state.addresses.isEmpty() -> ErrorScreen(
                    message = state.error,
                    onRetry = onRetry
                )

                state.addresses.isEmpty() -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.saved_addresses_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = stringResource(R.string.saved_addresses_empty_message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.addresses, key = { it.id ?: it.hashCode() }) { address ->
                        AddressCard(
                            address = address,
                            isUpdatingDefault = state.updatingDefaultId == address.id,
                            onEditClick = { onEditClick(address) },
                            onDeleteClick = { onDeleteClick(address) },
                            onSetDefaultClick = { onSetDefaultClick(address) },
                            enabled = state.isOnline,
                        )
                    }
                }
            }
        }

        PrimaryButton(
            text = stringResource(R.string.saved_addresses_add_new),
            onClick = onAddClick,
            enabled = state.isOnline,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
    }

    if (state.isFormSheetVisible) {
        AddressFormSheet(
            initialAddress = state.editingAddress,
            isSaving = state.isSaving,
            formError = state.formError,
            selectedLatLng = state.selectedLatLng,
            onMapClick = onMapClick,
            onClearSelectedLatLng = onClearSelectedLatLng,
            onDismissRequest = onDismissFormSheet,
            onSave = onSaveAddress,
        )
    }

    state.deleteCandidate?.let {
        DeleteAddressDialog(
            isDeleting = state.isDeleting,
            onDismiss = onDismissDeleteDialog,
            onConfirm = onConfirmDelete,
        )
    }
}

@Preview
@Composable
private fun SavedAddressesPreview() {
    AppTheme {
        SavedAddressesContent(
            state = SavedAddressesState(
                addresses = listOf(
                    Address(
                        id = "1",
                        firstName = "Ahmed",
                        lastName = "Hassan",
                        address1 = "15 El Nasr Road",
                        city = "Port Said",
                        country = "Egypt",
                        phone = "+201001234567",
                        isDefault = true,
                    )
                )
            ),
            onBackClick = {},
            onAddClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onSetDefaultClick = {},
            onRetry = {},
            onDismissFormSheet = {},
            onSaveAddress = {},
            onDismissDeleteDialog = {},
            onConfirmDelete = {},
            onMapClick = {},
            onDismissMap = {},
            onLocationSelected = {},
            onClearSelectedLatLng = {},
        )
    }
}
