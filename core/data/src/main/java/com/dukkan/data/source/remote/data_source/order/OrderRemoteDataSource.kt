package com.dukkan.data.source.remote.data_source.order

import com.dukkan.GetCustomerOrdersQuery

interface OrderRemoteDataSource {
    suspend fun getCustomerOrders(
        customerAccessToken: String,
        first: Int,
        after: String?
    ): Result<GetCustomerOrdersQuery.Customer>
}
