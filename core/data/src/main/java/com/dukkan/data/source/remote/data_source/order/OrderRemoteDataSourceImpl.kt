package com.dukkan.data.source.remote.data_source.order

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.dukkan.GetCustomerOrdersQuery
import javax.inject.Inject

class OrderRemoteDataSourceImpl @Inject constructor(
    private val apolloClient: ApolloClient
) : OrderRemoteDataSource {
    override suspend fun getCustomerOrders(
        customerAccessToken: String,
        first: Int,
        after: String?
    ): Result<GetCustomerOrdersQuery.Customer> {
        return try {
            val response = apolloClient.query(
                GetCustomerOrdersQuery(
                    customerAccessToken = customerAccessToken,
                    first = first,
                    after = Optional.presentIfNotNull(after)
                )
            ).execute()

            if (response.hasErrors()) {
                Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Unknown GraphQL Error"))
            } else {
                val customer = response.data?.customer
                if (customer != null) {
                    Result.success(customer)
                } else {
                    Result.failure(Exception("Customer not found or invalid token"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
