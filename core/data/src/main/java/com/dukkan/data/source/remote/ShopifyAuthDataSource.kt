package com.dukkan.data.source.remote

import com.apollographql.apollo.ApolloClient
import com.dukkan.CreateCustomerAccessTokenMutation
import com.dukkan.CustomerCreateMutation
import com.dukkan.CustomerAccessTokenCreateWithMultipassMutation
import com.dukkan.RenewCustomerAccessTokenMutation
import com.dukkan.data.source.remote.dto.ShopifyCustomerTokenDto
import com.dukkan.type.CustomerCreateInput

interface ShopifyAuthDataSource {
    suspend fun createCustomerToken(email: String, password: String): ShopifyCustomerTokenDto?
    suspend fun renewCustomerToken(token: String): ShopifyCustomerTokenDto?
    suspend fun createShopifyCustomer(email: String, password: String, firstName: String = "", lastName: String = ""): Boolean
    suspend fun createCustomerTokenWithMultipass(multipassToken: String): ShopifyCustomerTokenDto?
}

class ShopifyAuthDataSourceImpl(
    private val apolloClient: ApolloClient,
) : ShopifyAuthDataSource {

    override suspend fun createShopifyCustomer(email: String, password: String, firstName: String, lastName: String): Boolean {
        return try {
            val input = CustomerCreateInput(
                email = email,
                password = password,
                firstName = com.apollographql.apollo.api.Optional.presentIfNotNull(firstName.takeIf { it.isNotBlank() }),
                lastName = com.apollographql.apollo.api.Optional.presentIfNotNull(lastName.takeIf { it.isNotBlank() })
            )
            val response = apolloClient
                .mutation(CustomerCreateMutation(input = input))
                .execute()
            val payload = response.data?.customerCreate
            if (payload == null) {
                return false
            }
            if (payload.customerUserErrors.isEmpty()) return true
            
            payload.customerUserErrors.any { error ->
                val code = error.code?.name ?: error.code?.toString() ?: ""
                code == "CUSTOMER_DISABLED" || code == "TAKEN"
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun createCustomerToken(email: String, password: String): ShopifyCustomerTokenDto? {
        return try {
            val response = apolloClient
                .mutation(CreateCustomerAccessTokenMutation(email = email, password = password))
                .execute()
            val payload = response.data?.customerAccessTokenCreate
            if (payload == null) {
                return null
            }
            if (payload.customerUserErrors.isNotEmpty()) {
                return null
            }
            val token = payload.customerAccessToken ?: return null
            ShopifyCustomerTokenDto(
                accessToken = token.accessToken,
                expiresAt = token.expiresAt.toString(),
            )
        } catch (e: Exception) {
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

    override suspend fun createCustomerTokenWithMultipass(multipassToken: String): ShopifyCustomerTokenDto? {
        return try {
            val response = apolloClient
                .mutation(CustomerAccessTokenCreateWithMultipassMutation(multipassToken = multipassToken))
                .execute()
            val payload = response.data?.customerAccessTokenCreateWithMultipass ?: return null
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
}
