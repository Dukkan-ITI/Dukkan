package com.dukkan.address.viewmodel

import com.msayeh.domain.model.Address

data class SavedAddressesState(
    val isLoading: Boolean = false,
    val addresses: List<Address> = emptyList(),
    val error: String? = null,
    val isFormSheetVisible: Boolean = false,
    val editingAddress: Address? = null,
    val isSaving: Boolean = false,
    val formError: String? = null,
    val deleteCandidate: Address? = null,
    val isDeleting: Boolean = false,
    val updatingDefaultId: String? = null,
)
