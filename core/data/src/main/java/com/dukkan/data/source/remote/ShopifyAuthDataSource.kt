package com.dukkan.data.source.remote

import com.apollographql.apollo.ApolloClient
import com.dukkan.CreateCustomerAccessTokenMutation
import com.dukkan.RenewCustomerAccessTokenMutation
import com.dukkan.data.source.remote.dto.ShopifyCustomerTokenDto

interface ShopifyAuthDataSource {
    suspend fun createCustomerToken(email: String, password: String): ShopifyCustomerTokenDto?
    suspend fun renewCustomerToken(token: String): ShopifyCustomerTokenDto?
}

class ShopifyAuthDataSourceImpl(
    private val apolloClient: ApolloClient,
) : ShopifyAuthDataSource {

    override suspend fun createCustomerToken(email: String, password: String): ShopifyCustomerTokenDto? {
        return try {
            val response = apolloClient
                .mutation(CreateCustomerAccessTokenMutation(email = email, password = password))
                .execute()
            val payload = response.data?.customerAccessTokenCreate ?: return null
            if (payload.customerUserErrors.isNotEmpty()) return null
            val token = payload.customerAccessToken ?: return null
            ShopifyCustomerTokenDto(
                accessToken = token.accessToken,
                expiresAt = token.expiresAt.toString(),
            )
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun renewCustomerToken(token: String): ShopifyCustomerTokenDto? {
        return try {
            val response = apolloClient
                .mutation(RenewCustomerAccessTokenMutation(customerAccessToken = token))
                .execute()
            val payload = response.data?.customerAccessTokenRenew ?: return null
            if (payload.userErrors.isNotEmpty()) return null
            val renewed = payload.customerAccessToken ?: return null
            ShopifyCustomerTokenDto(
                accessToken = renewed.accessToken,
                expiresAt = renewed.expiresAt.toString(),
            )
        } catch (_: Exception) {
            null
        }
    }
}
