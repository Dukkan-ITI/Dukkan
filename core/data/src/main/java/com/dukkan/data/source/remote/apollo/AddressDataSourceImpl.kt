package com.dukkan.data.source.remote.apollo

import com.apollographql.apollo.ApolloClient
import com.dukkan.CustomerAddressCreateMutation
import com.dukkan.CustomerAddressDeleteMutation
import com.dukkan.CustomerAddressUpdateMutation
import com.dukkan.CustomerAddressesQuery
import com.dukkan.CustomerDefaultAddressUpdateMutation
import com.dukkan.type.MailingAddressInput
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressDataSourceImpl @Inject constructor(
    private val apolloClient: ApolloClient,
) : AddressDataSource {

    override suspend fun getAddresses(customerAccessToken: String): CustomerAddressesQuery.Customer? {
        val response = apolloClient.query(CustomerAddressesQuery(customerAccessToken)).execute()
        if (response.hasErrors()) {
            throw Exception(response.errors?.first()?.message ?: "Unknown GraphQL Error")
        }
        return response.data?.customer
    }

    override suspend fun createAddress(
        customerAccessToken: String,
        address: MailingAddressInput,
    ): CustomerAddressCreateMutation.CustomerAddressCreate? {
        val response = apolloClient.mutation(
            CustomerAddressCreateMutation(customerAccessToken, address)
        ).execute()
        if (response.hasErrors()) {
            throw Exception(response.errors?.first()?.message ?: "Unknown GraphQL Error")
        }
        return response.data?.customerAddressCreate
    }

    override suspend fun updateAddress(
        customerAccessToken: String,
        id: String,
        address: MailingAddressInput,
    ): CustomerAddressUpdateMutation.CustomerAddressUpdate? {
        val response = apolloClient.mutation(
            CustomerAddressUpdateMutation(customerAccessToken, id, address)
        ).execute()
        if (response.hasErrors()) {
            throw Exception(response.errors?.first()?.message ?: "Unknown GraphQL Error")
        }
        return response.data?.customerAddressUpdate
    }

    override suspend fun deleteAddress(
        customerAccessToken: String,
        id: String,
    ): CustomerAddressDeleteMutation.CustomerAddressDelete? {
        val response = apolloClient.mutation(
            CustomerAddressDeleteMutation(customerAccessToken, id)
        ).execute()
        if (response.hasErrors()) {
            throw Exception(response.errors?.first()?.message ?: "Unknown GraphQL Error")
        }
        return response.data?.customerAddressDelete
    }

    override suspend fun setDefaultAddress(
        customerAccessToken: String,
        addressId: String,
    ): CustomerDefaultAddressUpdateMutation.CustomerDefaultAddressUpdate? {
        val response = apolloClient.mutation(
            CustomerDefaultAddressUpdateMutation(customerAccessToken, addressId)
        ).execute()
        if (response.hasErrors()) {
            throw Exception(response.errors?.first()?.message ?: "Unknown GraphQL Error")
        }
        return response.data?.customerDefaultAddressUpdate
    }
}
