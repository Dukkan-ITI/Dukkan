package com.dukkan.domain.usecase.address

import com.dukkan.domain.repository.AddressRepository
import javax.inject.Inject

class SetDefaultAddressUseCase @Inject constructor(
    private val addressRepository: AddressRepository,
) {
    suspend operator fun invoke(addressId: String): Result<Unit> =
        addressRepository.setDefaultAddress(addressId)
}
