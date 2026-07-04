package com.dukkan.data.source.remote.apollo

import com.dukkan.CustomerAddressCreateMutation
import com.dukkan.CustomerAddressDeleteMutation
import com.dukkan.CustomerAddressUpdateMutation
import com.dukkan.CustomerAddressesQuery
import com.dukkan.CustomerDefaultAddressUpdateMutation
import com.dukkan.type.MailingAddressInput

interface AddressDataSource {
    suspend fun getAddresses(customerAccessToken: String): CustomerAddressesQuery.Customer?

    suspend fun createAddress(
        customerAccessToken: String,
        address: MailingAddressInput,
    ): CustomerAddressCreateMutation.CustomerAddressCreate?

    suspend fun updateAddress(
        customerAccessToken: String,
        id: String,
        address: MailingAddressInput,
    ): CustomerAddressUpdateMutation.CustomerAddressUpdate?

    suspend fun deleteAddress(
        customerAccessToken: String,
        id: String,
    ): CustomerAddressDeleteMutation.CustomerAddressDelete?

    suspend fun setDefaultAddress(
        customerAccessToken: String,
        addressId: String,
    ): CustomerDefaultAddressUpdateMutation.CustomerDefaultAddressUpdate?
}
