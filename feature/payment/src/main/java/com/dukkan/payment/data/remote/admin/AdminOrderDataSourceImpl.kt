package com.dukkan.payment.data.remote.admin

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.dukkan.payment.admin.OrderCancelMutation
import com.dukkan.payment.admin.OrderCreateMutation
import com.dukkan.payment.admin.OrderMarkAsPaidMutation
import com.dukkan.payment.admin.type.OrderCancelReason
import com.dukkan.payment.admin.type.OrderCreateOrderInput
import com.dukkan.payment.admin.type.OrderCreateOptionsInput
import com.dukkan.payment.admin.type.OrderMarkAsPaidInput
import com.dukkan.payment.di.AdminApollo
import javax.inject.Inject

internal class AdminOrderDataSourceImpl @Inject constructor(
    @AdminApollo
    private val apolloClient: ApolloClient,
) : AdminOrderDataSource {

    override suspend fun createOrder(
        order: OrderCreateOrderInput,
        options: OrderCreateOptionsInput?,
    ): OrderCreateMutation.OrderCreate {
        val response = apolloClient.mutation(
            OrderCreateMutation(
                order = order,
                options = Optional.presentIfNotNull(options),
            )
        ).execute()

        if (response.hasErrors()) {
            throw Exception(response.errors?.firstOrNull()?.message ?: "Unknown GraphQL Error")
        }

        return response.data?.orderCreate ?: throw Exception("Order creation returned no data")
    }

    override suspend fun markOrderAsPaid(input: OrderMarkAsPaidInput): OrderMarkAsPaidMutation.OrderMarkAsPaid {
        val response = apolloClient.mutation(OrderMarkAsPaidMutation(input)).execute()

        if (response.hasErrors()) {
            throw Exception(response.errors?.firstOrNull()?.message ?: "Unknown GraphQL Error")
        }

        return response.data?.orderMarkAsPaid ?: throw Exception("Mark order paid returned no data")
    }

    override suspend fun cancelOrder(
        orderId: String,
        reason: OrderCancelReason,
        restock: Boolean,
    ): OrderCancelMutation.OrderCancel {
        val response = apolloClient.mutation(
            OrderCancelMutation(
                orderId = orderId,
                reason = reason,
                restock = restock,
            )
        ).execute()

        if (response.hasErrors()) {
            throw Exception(response.errors?.firstOrNull()?.message ?: "Unknown GraphQL Error")
        }

        return response.data?.orderCancel ?: throw Exception("Order cancel returned no data")
    }
}
