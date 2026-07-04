package com.msayeh.domain.repository

import com.msayeh.domain.model.Address

interface AddressRepository {
    suspend fun getAddresses(): Result<List<Address>>
    suspend fun addAddress(address: Address): Result<Address>
    suspend fun updateAddress(address: Address): Result<Address>
    suspend fun deleteAddress(addressId: String): Result<String>
    suspend fun setDefaultAddress(addressId: String): Result<Unit>
}