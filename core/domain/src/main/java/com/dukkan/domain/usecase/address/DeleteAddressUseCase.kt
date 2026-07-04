package com.dukkan.domain.usecase.address

import com.dukkan.domain.repository.AddressRepository
import javax.inject.Inject

class DeleteAddressUseCase @Inject constructor(
    private val addressRepository: AddressRepository,
) {
    suspend operator fun invoke(addressId: String): Result<String> =
        addressRepository.deleteAddress(addressId)
}
