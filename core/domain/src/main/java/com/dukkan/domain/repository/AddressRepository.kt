package com.dukkan.domain.repository

import com.dukkan.domain.model.Address

interface AddressRepository {
    suspend fun getAddresses(): Result<List<Address>>
    suspend fun addAddress(address: Address): Result<Address>
    suspend fun updateAddress(address: Address): Result<Address>
    suspend fun deleteAddress(addressId: String): Result<String>
    suspend fun setDefaultAddress(addressId: String): Result<Unit>
}
