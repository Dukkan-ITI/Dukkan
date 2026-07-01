package com.dukkan.data.source.remote.data_source.cart

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.dukkan.AddCartLinesMutation
import com.dukkan.ApplyDiscountCodeMutation
import com.dukkan.CreateCartMutation
import com.dukkan.GetCartQuery
import com.dukkan.RemoveCartLinesMutation
import com.dukkan.UpdateCartLinesMutation
import com.dukkan.type.CartInput
import com.dukkan.type.CartBuyerIdentityInput
import com.dukkan.type.CartLineInput
import com.dukkan.type.CartLineUpdateInput
import javax.inject.Inject

class CartRemoteDataSourceImpl @Inject constructor(
    private val apolloClient: ApolloClient
) : CartRemoteDataSource {

    override suspend fun createCart(customerAccessToken: String?): CreateCartMutation.CartCreate? {
        val buyerIdentity = customerAccessToken?.let {
            CartBuyerIdentityInput(
                customerAccessToken = Optional.present(it)
            )
        }
        val input = CartInput(
            buyerIdentity = Optional.presentIfNotNull(buyerIdentity)
        )
        val response = apolloClient.mutation(CreateCartMutation(input)).execute()
        return response.data?.cartCreate
    }

    override suspend fun getCart(cartId: String): GetCartQuery.Cart? {
        val response = apolloClient.query(GetCartQuery(cartId)).execute()
        return response.data?.cart
    }

    override suspend fun addCartItem(cartId: String, variantId: String): AddCartLinesMutation.CartLinesAdd? {
        val line = CartLineInput(
            merchandiseId = variantId,
            quantity = Optional.present(1)
        )
        val response = apolloClient.mutation(AddCartLinesMutation(cartId, listOf(line))).execute()
        return response.data?.cartLinesAdd
    }

    override suspend fun updateCartItem(
        cartId: String,
        lineId: String,
        quantity: Int
    ): UpdateCartLinesMutation.CartLinesUpdate? {
        val line = CartLineUpdateInput(
            id = lineId,
            quantity = Optional.present(quantity)
        )
        val response = apolloClient.mutation(UpdateCartLinesMutation(cartId, listOf(line))).execute()
        return response.data?.cartLinesUpdate
    }

    override suspend fun removeCartItem(
        cartId: String,
        lineId: String
    ): RemoveCartLinesMutation.CartLinesRemove? {
        val response = apolloClient.mutation(RemoveCartLinesMutation(cartId, listOf(lineId))).execute()
        return response.data?.cartLinesRemove
    }

    override suspend fun applyDiscountCode(
        cartId: String,
        discountCode: String
    ): ApplyDiscountCodeMutation.CartDiscountCodesUpdate? {
        val response = apolloClient.mutation(
            ApplyDiscountCodeMutation(cartId, listOf(discountCode))
        ).execute()
        return response.data?.cartDiscountCodesUpdate
    }
}
