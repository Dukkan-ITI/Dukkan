package com.msayeh.domain.usecase.address

import com.msayeh.domain.model.Address
import com.msayeh.domain.repository.AddressRepository
import javax.inject.Inject

class AddAddressUseCase @Inject constructor(
    private val addressRepository: AddressRepository,
) {
    suspend operator fun invoke(address: Address): Result<Address> = addressRepository.addAddress(address)
}
