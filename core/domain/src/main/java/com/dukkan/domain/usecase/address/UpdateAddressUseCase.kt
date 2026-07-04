package com.dukkan.domain.usecase.address

import com.dukkan.domain.model.Address
import com.dukkan.domain.repository.AddressRepository
import javax.inject.Inject

class UpdateAddressUseCase @Inject constructor(
    private val addressRepository: AddressRepository,
) {
    suspend operator fun invoke(address: Address): Result<Address> =
        addressRepository.updateAddress(address)
}
