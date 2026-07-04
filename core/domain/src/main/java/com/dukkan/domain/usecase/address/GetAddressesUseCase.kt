package com.dukkan.domain.usecase.address

import com.dukkan.domain.model.Address
import com.dukkan.domain.repository.AddressRepository
import javax.inject.Inject

class GetAddressesUseCase @Inject constructor(
    private val addressRepository: AddressRepository,
) {
    suspend operator fun invoke(): Result<List<Address>> = addressRepository.getAddresses()
}
