package com.dukkan.address.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.domain.model.Address
import com.dukkan.domain.usecase.auth.GetCurrentUserUseCase
import com.dukkan.domain.usecase.address.AddAddressUseCase
import com.dukkan.domain.usecase.address.DeleteAddressUseCase
import com.dukkan.domain.usecase.address.GetAddressesUseCase
import com.dukkan.domain.usecase.address.SetDefaultAddressUseCase
import com.dukkan.domain.usecase.address.UpdateAddressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedAddressesViewModel @Inject constructor(
    private val getAddresses: GetAddressesUseCase,
    private val addAddress: AddAddressUseCase,
    private val updateAddress: UpdateAddressUseCase,
    private val deleteAddress: DeleteAddressUseCase,
    private val setDefaultAddress: SetDefaultAddressUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SavedAddressesState())
    val state: StateFlow<SavedAddressesState> = _state.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        viewModelScope.launch {
            _isLoggedIn.value = getCurrentUser() != null
            if (_isLoggedIn.value) loadAddresses()
        }
    }

    fun loadAddresses() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            getAddresses()
                .onSuccess { list ->
                    _state.value = _state.value.copy(isLoading = false, addresses = list)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    fun onAddClick() {
        _state.value =
            _state.value.copy(isFormSheetVisible = true, editingAddress = null, formError = null)
    }

    fun onEditClick(address: Address) {
        _state.value =
            _state.value.copy(isFormSheetVisible = true, editingAddress = address, formError = null)
    }

    fun dismissFormSheet() {
        _state.value =
            _state.value.copy(isFormSheetVisible = false, editingAddress = null, formError = null)
    }

    fun onMapClick() {
        _state.value = _state.value.copy(isMapVisible = true)
    }

    fun dismissMap() {
        _state.value = _state.value.copy(isMapVisible = false)
    }

    fun onLocationSelected(latLng: com.google.android.gms.maps.model.LatLng) {
        _state.value = _state.value.copy(isMapVisible = false, selectedLatLng = latLng)
    }

    fun clearSelectedLatLng() {
        _state.value = _state.value.copy(selectedLatLng = null)
    }

    fun saveAddress(address: Address) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, formError = null)
            val result = if (address.id == null) addAddress(address) else updateAddress(address)
            result
                .onSuccess {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        isFormSheetVisible = false,
                        editingAddress = null
                    )
                    loadAddresses()
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isSaving = false, formError = e.message)
                }
        }
    }

    fun onDeleteClick(address: Address) {
        _state.value = _state.value.copy(deleteCandidate = address)
    }

    fun dismissDeleteDialog() {
        _state.value = _state.value.copy(deleteCandidate = null)
    }

    fun confirmDelete() {
        val target = _state.value.deleteCandidate ?: return
        val targetId = target.id ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isDeleting = true)
            deleteAddress(targetId)
                .onSuccess {
                    _state.value = _state.value.copy(isDeleting = false, deleteCandidate = null)
                    loadAddresses()
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isDeleting = false,
                        deleteCandidate = null,
                        error = e.message
                    )
                }
        }
    }

    fun onSetDefaultClick(address: Address) {
        val id = address.id ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(updatingDefaultId = id)
            setDefaultAddress(id)
                .onSuccess {
                    _state.value = _state.value.copy(updatingDefaultId = null)
                    loadAddresses()
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(updatingDefaultId = null, error = e.message)
                }
        }
    }
}
