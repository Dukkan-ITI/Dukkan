package com.dukkan.data.repository

import com.dukkan.data.mapper.toDomainModel
import com.dukkan.data.mapper.toMailingAddressInput
import com.dukkan.data.source.local.ShopifyTokenStore
import com.dukkan.data.source.remote.apollo.AddressDataSource
import com.dukkan.domain.model.Address
import com.dukkan.domain.repository.AddressRepository
import javax.inject.Inject

class AddressRepositoryImpl @Inject constructor(
    private val addressDataSource: AddressDataSource,
    private val tokenStore: ShopifyTokenStore,
) : AddressRepository {

    private suspend fun requireToken(): Result<String> {
        val token = tokenStore.getToken()?.accessToken
        return if (token != null) Result.success(token) else Result.failure(Exception("Not authenticated"))
    }

    override suspend fun getAddresses(): Result<List<Address>> {
        val token = requireToken().getOrElse { return Result.failure(it) }
        return try {
            val customer = addressDataSource.getAddresses(token)
                ?: return Result.failure(Exception("Unable to load addresses"))
            val defaultId = customer.defaultAddress?.id
            val addresses = customer.addresses.edges.map { it.node.toDomainModel(defaultId) }
            Result.success(addresses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addAddress(address: Address): Result<Address> {
        val token = requireToken().getOrElse { return Result.failure(it) }
        return try {
            val payload = addressDataSource.createAddress(token, address.toMailingAddressInput())
                ?: return Result.failure(Exception("Unable to add address"))
            payload.customerUserErrors.firstOrNull()
                ?.let { return Result.failure(Exception(it.message)) }
            val created = payload.customerAddress?.toDomainModel()
                ?: return Result.failure(Exception("Unable to add address"))
            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAddress(address: Address): Result<Address> {
        val id =
            address.id ?: return Result.failure(IllegalArgumentException("Address id is required"))
        val token = requireToken().getOrElse { return Result.failure(it) }
        return try {
            val payload =
                addressDataSource.updateAddress(token, id, address.toMailingAddressInput())
                    ?: return Result.failure(Exception("Unable to update address"))
            payload.customerUserErrors.firstOrNull()
                ?.let { return Result.failure(Exception(it.message)) }
            val updated = payload.customerAddress?.toDomainModel()
                ?: return Result.failure(Exception("Unable to update address"))
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAddress(addressId: String): Result<String> {
        val token = requireToken().getOrElse { return Result.failure(it) }
        return try {
            val payload = addressDataSource.deleteAddress(token, addressId)
                ?: return Result.failure(Exception("Unable to delete address"))
            payload.customerUserErrors.firstOrNull()
                ?.let { return Result.failure(Exception(it.message)) }
            val deletedId = payload.deletedCustomerAddressId
                ?: return Result.failure(Exception("Unable to delete address"))
            Result.success(deletedId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setDefaultAddress(addressId: String): Result<Unit> {
        val token = requireToken().getOrElse { return Result.failure(it) }
        return try {
            val payload = addressDataSource.setDefaultAddress(token, addressId)
                ?: return Result.failure(Exception("Unable to set default address"))
            payload.customerUserErrors.firstOrNull()
                ?.let { return Result.failure(Exception(it.message)) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
