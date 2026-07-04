package com.msayeh.domain.usecase.address

import com.msayeh.domain.repository.AddressRepository
import javax.inject.Inject

class DeleteAddressUseCase @Inject constructor(
    private val addressRepository: AddressRepository,
) {
    suspend operator fun invoke(addressId: String): Result<String> = addressRepository.deleteAddress(addressId)
}
