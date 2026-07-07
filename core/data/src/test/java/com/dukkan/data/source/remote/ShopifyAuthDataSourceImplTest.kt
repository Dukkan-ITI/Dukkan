package com.dukkan.data.source.remote

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.ApolloCall
import com.dukkan.CreateCustomerAccessTokenMutation
import com.dukkan.CustomerCreateMutation
import com.dukkan.CustomerAccessTokenCreateWithMultipassMutation
import com.dukkan.RenewCustomerAccessTokenMutation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import com.dukkan.type.CustomerCreateInput
import com.benasher44.uuid.uuid4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import com.dukkan.type.CustomerErrorCode

class ShopifyAuthDataSourceImplTest {

    private lateinit var dataSource: ShopifyAuthDataSourceImpl
    private val apolloClient: ApolloClient = mockk()

    @Before
    fun setUp() {
        dataSource = ShopifyAuthDataSourceImpl(apolloClient)
    }

    @Test
    fun `createShopifyCustomer returns true when customer is created successfully`() = runTest {
        // Arrange
        val mockPayload = CustomerCreateMutation.CustomerCreate(
            customer = null,
            customerUserErrors = emptyList(),
        )
        val mockData = CustomerCreateMutation.Data(customerCreate = mockPayload)
        val mockResponse = ApolloResponse.Builder(
            operation = CustomerCreateMutation(
                input = CustomerCreateInput(
                    email = "test@test.com",
                    password = "password",
                    firstName = com.apollographql.apollo.api.Optional.presentIfNotNull(null),
                    lastName = com.apollographql.apollo.api.Optional.presentIfNotNull(null),
                )
            ),
            requestUuid = uuid4(),
        ).data(mockData).build()

        val mockCall = mockk<ApolloCall<CustomerCreateMutation.Data>>()
        every { apolloClient.mutation(any<CustomerCreateMutation>()) } returns mockCall
        coEvery { mockCall.execute() } returns mockResponse

        // Act
        val result = dataSource.createShopifyCustomer("test@test.com", "password")

        // Assert
        assertTrue(result)
    }

    @Test
    fun `createShopifyCustomer returns true when customerUserErrors contains TAKEN`() = runTest {
        // Arrange
        val mockError = CustomerCreateMutation.CustomerUserError(
            code = CustomerErrorCode.TAKEN,
            field = null,
            message = "taken",
        )
        val mockPayload = CustomerCreateMutation.CustomerCreate(
            customer = null,
            customerUserErrors = listOf(mockError),
        )
        val mockData = CustomerCreateMutation.Data(customerCreate = mockPayload)
        val mockResponse = ApolloResponse.Builder(
            operation = CustomerCreateMutation(
                input = CustomerCreateInput(
                    email = "test@test.com",
                    password = "password",
                    firstName = com.apollographql.apollo.api.Optional.presentIfNotNull(null),
                    lastName = com.apollographql.apollo.api.Optional.presentIfNotNull(null),
                )
            ),
            requestUuid = uuid4(),
        ).data(mockData).build()

        val mockCall = mockk<ApolloCall<CustomerCreateMutation.Data>>()
        every { apolloClient.mutation(any<CustomerCreateMutation>()) } returns mockCall
        coEvery { mockCall.execute() } returns mockResponse

        // Act
        val result = dataSource.createShopifyCustomer("test@test.com", "password")

        // Assert
        assertTrue(result)
    }

    @Test
    fun `createShopifyCustomer returns true when customerUserErrors contains CUSTOMER_DISABLED`() = runTest {
        // Arrange
        val mockError = CustomerCreateMutation.CustomerUserError(
            code = CustomerErrorCode.CUSTOMER_DISABLED,
            field = null,
            message = "disabled",
        )
        val mockPayload = CustomerCreateMutation.CustomerCreate(
            customer = null,
            customerUserErrors = listOf(mockError),
        )
        val mockData = CustomerCreateMutation.Data(customerCreate = mockPayload)
        val mockResponse = ApolloResponse.Builder(
            operation = CustomerCreateMutation(
                input = CustomerCreateInput(
                    email = "test@test.com",
                    password = "password",
                    firstName = com.apollographql.apollo.api.Optional.presentIfNotNull(null),
                    lastName = com.apollographql.apollo.api.Optional.presentIfNotNull(null),
                )
            ),
            requestUuid = uuid4(),
        ).data(mockData).build()

        val mockCall = mockk<ApolloCall<CustomerCreateMutation.Data>>()
        every { apolloClient.mutation(any<CustomerCreateMutation>()) } returns mockCall
        coEvery { mockCall.execute() } returns mockResponse

        // Act
        val result = dataSource.createShopifyCustomer("test@test.com", "password")

        // Assert
        assertTrue(result)
    }

    @Test
    fun `createShopifyCustomer returns false when payload is null`() = runTest {
        // Arrange
        val mockData = CustomerCreateMutation.Data(customerCreate = null)
        val mockResponse = ApolloResponse.Builder(
            operation = CustomerCreateMutation(
                input = CustomerCreateInput(
                    email = "test@test.com",
                    password = "password",
                    firstName = com.apollographql.apollo.api.Optional.presentIfNotNull(null),
                    lastName = com.apollographql.apollo.api.Optional.presentIfNotNull(null),
                )
            ),
            requestUuid = uuid4(),
        ).data(mockData).build()

        val mockCall = mockk<ApolloCall<CustomerCreateMutation.Data>>()
        every { apolloClient.mutation(any<CustomerCreateMutation>()) } returns mockCall
        coEvery { mockCall.execute() } returns mockResponse

        // Act
        val result = dataSource.createShopifyCustomer("test@test.com", "password")

        // Assert
        assertFalse(result)
    }

    @Test
    fun `createShopifyCustomer returns false when an exception is thrown`() = runTest {
        // Arrange
        every { apolloClient.mutation(any<CustomerCreateMutation>()) } throws Exception("Apollo error")

        // Act
        val result = dataSource.createShopifyCustomer("test@test.com", "password")

        // Assert
        assertFalse(result)
    }

    @Test
    fun `createShopifyCustomer returns false for any other customerUserErrors`() = runTest {
        // Arrange
        val mockError = CustomerCreateMutation.CustomerUserError(
            code = CustomerErrorCode.INVALID,
            field = null,
            message = "invalid",
        )
        val mockPayload = CustomerCreateMutation.CustomerCreate(
            customer = null,
            customerUserErrors = listOf(mockError),
        )
        val mockData = CustomerCreateMutation.Data(customerCreate = mockPayload)
        val mockResponse = ApolloResponse.Builder(
            operation = CustomerCreateMutation(
                input = CustomerCreateInput(
                    email = "test@test.com",
                    password = "password",
                    firstName = com.apollographql.apollo.api.Optional.presentIfNotNull(null),
                    lastName = com.apollographql.apollo.api.Optional.presentIfNotNull(null),
                )
            ),
            requestUuid = uuid4(),
        ).data(mockData).build()

        val mockCall = mockk<ApolloCall<CustomerCreateMutation.Data>>()
        every { apolloClient.mutation(any<CustomerCreateMutation>()) } returns mockCall
        coEvery { mockCall.execute() } returns mockResponse

        // Act
        val result = dataSource.createShopifyCustomer("test@test.com", "password")

        // Assert
        assertFalse(result)
    }

    @Test
    fun `createCustomerToken returns ShopifyCustomerTokenDto on success`() = runTest {
        // Arrange
        val mockToken = CreateCustomerAccessTokenMutation.CustomerAccessToken(
            accessToken = "mock_token",
            expiresAt = "2024-12-31T23:59:59Z",
        )
        val mockPayload = CreateCustomerAccessTokenMutation.CustomerAccessTokenCreate(
            customerAccessToken = mockToken,
            customerUserErrors = emptyList(),
        )
        val mockData = CreateCustomerAccessTokenMutation.Data(customerAccessTokenCreate = mockPayload)
        val mockResponse = ApolloResponse.Builder(
            operation = CreateCustomerAccessTokenMutation(email = "test@test.com", password = "password"),
            requestUuid = uuid4(),
        ).data(mockData).build()

        val mockCall = mockk<ApolloCall<CreateCustomerAccessTokenMutation.Data>>()
        every { apolloClient.mutation(any<CreateCustomerAccessTokenMutation>()) } returns mockCall
        coEvery { mockCall.execute() } returns mockResponse

        // Act
        val result = dataSource.createCustomerToken("test@test.com", "password")

        // Assert
        assertEquals("mock_token", result?.accessToken)
        assertEquals("2024-12-31T23:59:59Z", result?.expiresAt)
    }

    @Test
    fun `createCustomerToken returns null when payload is null`() = runTest {
        // Arrange
        val mockData = CreateCustomerAccessTokenMutation.Data(customerAccessTokenCreate = null)
        val mockResponse = ApolloResponse.Builder(
            operation = CreateCustomerAccessTokenMutation(email = "test@test.com", password = "password"),
            requestUuid = uuid4(),
        ).data(mockData).build()

        val mockCall = mockk<ApolloCall<CreateCustomerAccessTokenMutation.Data>>()
        every { apolloClient.mutation(any<CreateCustomerAccessTokenMutation>()) } returns mockCall
        coEvery { mockCall.execute() } returns mockResponse

        // Act
        val result = dataSource.createCustomerToken("test@test.com", "password")

        // Assert
        assertNull(result)
    }

    @Test
    fun `createCustomerToken returns null when customerUserErrors is not empty`() = runTest {
        // Arrange
        val mockPayload = CreateCustomerAccessTokenMutation.CustomerAccessTokenCreate(
            customerAccessToken = null,
            customerUserErrors = listOf(
                CreateCustomerAccessTokenMutation.CustomerUserError(
                    code = null,
                    field = null,
                    message = "error",
                )
            ),
        )
        val mockData = CreateCustomerAccessTokenMutation.Data(customerAccessTokenCreate = mockPayload)
        val mockResponse = ApolloResponse.Builder(
            operation = CreateCustomerAccessTokenMutation(email = "test@test.com", password = "password"),
            requestUuid = uuid4(),
        ).data(mockData).build()

        val mockCall = mockk<ApolloCall<CreateCustomerAccessTokenMutation.Data>>()
        every { apolloClient.mutation(any<CreateCustomerAccessTokenMutation>()) } returns mockCall
        coEvery { mockCall.execute() } returns mockResponse

        // Act
        val result = dataSource.createCustomerToken("test@test.com", "password")

        // Assert
        assertNull(result)
    }

    @Test
    fun `createCustomerToken returns null when customerAccessToken is null`() = runTest {
        // Arrange
        val mockPayload = CreateCustomerAccessTokenMutation.CustomerAccessTokenCreate(
            customerAccessToken = null,
            customerUserErrors = emptyList(),
        )
        val mockData = CreateCustomerAccessTokenMutation.Data(customerAccessTokenCreate = mockPayload)
        val mockResponse = ApolloResponse.Builder(
            operation = CreateCustomerAccessTokenMutation(email = "test@test.com", password = "password"),
            requestUuid = uuid4(),
        ).data(mockData).build()

        val mockCall = mockk<ApolloCall<CreateCustomerAccessTokenMutation.Data>>()
        every { apolloClient.mutation(any<CreateCustomerAccessTokenMutation>()) } returns mockCall
        coEvery { mockCall.execute() } returns mockResponse

        // Act
        val result = dataSource.createCustomerToken("test@test.com", "password")

        // Assert
        assertNull(result)
    }

    @Test
    fun `createCustomerToken returns null when Apollo throws an exception`() = runTest {
        // Arrange
        every { apolloClient.mutation(any<CreateCustomerAccessTokenMutation>()) } throws Exception("Apollo error")

        // Act
        val result = dataSource.createCustomerToken("test@test.com", "password")

        // Assert
        assertNull(result)
    }

    @Test
    fun `renewCustomerToken returns ShopifyCustomerTokenDto on success`() = runTest {
        // Arrange
        val mockToken = RenewCustomerAccessTokenMutation.CustomerAccessToken(
            accessToken = "renewed_token",
            expiresAt = "2024-12-31T23:59:59Z",
        )
        val mockPayload = RenewCustomerAccessTokenMutation.CustomerAccessTokenRenew(
            customerAccessToken = mockToken,
            userErrors = emptyList(),
        )
        val mockData = RenewCustomerAccessTokenMutation.Data(customerAccessTokenRenew = mockPayload)
        val mockResponse = ApolloResponse.Builder(
            operation = RenewCustomerAccessTokenMutation(customerAccessToken = "old_token"),
            requestUuid = uuid4(),
        ).data(mockData).build()

        val mockCall = mockk<ApolloCall<RenewCustomerAccessTokenMutation.Data>>()
        every { apolloClient.mutation(any<RenewCustomerAccessTokenMutation>()) } returns mockCall
        coEvery { mockCall.execute() } returns mockResponse

        // Act
        val result = dataSource.renewCustomerToken("old_token")

        // Assert
        assertEquals("renewed_token", result?.accessToken)
        assertEquals("2024-12-31T23:59:59Z", result?.expiresAt)
    }

    @Test
    fun `renewCustomerToken returns null when payload is null`() = runTest {
        // Arrange
        val mockData = RenewCustomerAccessTokenMutation.Data(customerAccessTokenRenew = null)
        val mockResponse = ApolloResponse.Builder(
            operation = RenewCustomerAccessTokenMutation(customerAccessToken = "old_token"),
            requestUuid = uuid4(),
        ).data(mockData).build()

        val mockCall = mockk<ApolloCall<RenewCustomerAccessTokenMutation.Data>>()
        every { apolloClient.mutation(any<RenewCustomerAccessTokenMutation>()) } returns mockCall
        coEvery { mockCall.execute() } returns mockResponse

        // Act
        val result = dataSource.renewCustomerToken("old_token")

        // Assert
        assertNull(result)
    }

    @Test
    fun `renewCustomerToken returns null when userErrors is not empty`() = runTest {
        // Arrange
        val mockPayload = RenewCustomerAccessTokenMutation.CustomerAccessTokenRenew(
            customerAccessToken = null,
            userErrors = listOf(
                RenewCustomerAccessTokenMutation.UserError(
                    field = null,
                    message = "error",
                )
            ),
        )
        val mockData = RenewCustomerAccessTokenMutation.Data(customerAccessTokenRenew = mockPayload)
        val mockResponse = ApolloResponse.Builder(
            operation = RenewCustomerAccessTokenMutation(customerAccessToken = "old_token"),
            requestUuid = uuid4(),
        ).data(mockData).build()

        val mockCall = mockk<ApolloCall<RenewCustomerAccessTokenMutation.Data>>()
        every { apolloClient.mutation(any<RenewCustomerAccessTokenMutation>()) } returns mockCall
        coEvery { mockCall.execute() } returns mockResponse

        // Act
        val result = dataSource.renewCustomerToken("old_token")

        // Assert
        assertNull(result)
    }

    @Test
    fun `renewCustomerToken returns null when customerAccessToken is null`() = runTest {
        // Arrange
        val mockPayload = RenewCustomerAccessTokenMutation.CustomerAccessTokenRenew(
            customerAccessToken = null,
            userErrors = emptyList(),
        )
        val mockData = RenewCustomerAccessTokenMutation.Data(customerAccessTokenRenew = mockPayload)
        val mockResponse = ApolloResponse.Builder(
            operation = RenewCustomerAccessTokenMutation(customerAccessToken = "old_token"),
            requestUuid = uuid4(),
        ).data(mockData).build()

        val mockCall = mockk<ApolloCall<RenewCustomerAccessTokenMutation.Data>>()
        every { apolloClient.mutation(any<RenewCustomerAccessTokenMutation>()) } returns mockCall
        coEvery { mockCall.execute() } returns mockResponse

        // Act
        val result = dataSource.renewCustomerToken("old_token")

        // Assert
        assertNull(result)
    }

    @Test
    fun `renewCustomerToken returns null when an exception is thrown`() = runTest {
        // Arrange
        every { apolloClient.mutation(any<RenewCustomerAccessTokenMutation>()) } throws Exception("Apollo error")

        // Act
        val result = dataSource.renewCustomerToken("old_token")

        // Assert
        assertNull(result)
    }

    @Test
    fun `createCustomerTokenWithMultipass returns ShopifyCustomerTokenDto on success`() = runTest {
        // Arrange
        val mockToken = CustomerAccessTokenCreateWithMultipassMutation.CustomerAccessToken(
            accessToken = "multipass_token",
            expiresAt = "2024-12-31T23:59:59Z",
        )
        val mockPayload = CustomerAccessTokenCreateWithMultipassMutation.CustomerAccessTokenCreateWithMultipass(
            customerAccessToken = mockToken,
            customerUserErrors = emptyList(),
        )
        val mockData = CustomerAccessTokenCreateWithMultipassMutation.Data(
            customerAccessTokenCreateWithMultipass = mockPayload
        )
        val mockResponse = ApolloResponse.Builder(
            operation = CustomerAccessTokenCreateWithMultipassMutation(multipassToken = "multipass_secret"),
            requestUuid = uuid4(),
        ).data(mockData).build()

        val mockCall = mockk<ApolloCall<CustomerAccessTokenCreateWithMultipassMutation.Data>>()
        every { apolloClient.mutation(any<CustomerAccessTokenCreateWithMultipassMutation>()) } returns mockCall
        coEvery { mockCall.execute() } returns mockResponse

        // Act
        val result = dataSource.createCustomerTokenWithMultipass("multipass_secret")

        // Assert
        assertEquals("multipass_token", result?.accessToken)
        assertEquals("2024-12-31T23:59:59Z", result?.expiresAt)
    }

    @Test
    fun `createCustomerTokenWithMultipass returns null when payload is null`() = runTest {
        // Arrange
        val mockData = CustomerAccessTokenCreateWithMultipassMutation.Data(
            customerAccessTokenCreateWithMultipass = null
        )
        val mockResponse = ApolloResponse.Builder(
            operation = CustomerAccessTokenCreateWithMultipassMutation(multipassToken = "multipass_secret"),
            requestUuid = uuid4(),
        ).data(mockData).build()

        val mockCall = mockk<ApolloCall<CustomerAccessTokenCreateWithMultipassMutation.Data>>()
        every { apolloClient.mutation(any<CustomerAccessTokenCreateWithMultipassMutation>()) } returns mockCall
        coEvery { mockCall.execute() } returns mockResponse

        // Act
        val result = dataSource.createCustomerTokenWithMultipass("multipass_secret")

        // Assert
        assertNull(result)
    }

    @Test
    fun `createCustomerTokenWithMultipass returns null when customerUserErrors is not empty`() = runTest {
        // Arrange
        val mockPayload = CustomerAccessTokenCreateWithMultipassMutation.CustomerAccessTokenCreateWithMultipass(
            customerAccessToken = null,
            customerUserErrors = listOf(
                CustomerAccessTokenCreateWithMultipassMutation.CustomerUserError(
                    code = null,
                    field = null,
                    message = "error",
                )
            ),
        )
        val mockData = CustomerAccessTokenCreateWithMultipassMutation.Data(
            customerAccessTokenCreateWithMultipass = mockPayload
        )
        val mockResponse = ApolloResponse.Builder(
            operation = CustomerAccessTokenCreateWithMultipassMutation(multipassToken = "multipass_secret"),
            requestUuid = uuid4(),
        ).data(mockData).build()

        val mockCall = mockk<ApolloCall<CustomerAccessTokenCreateWithMultipassMutation.Data>>()
        every { apolloClient.mutation(any<CustomerAccessTokenCreateWithMultipassMutation>()) } returns mockCall
        coEvery { mockCall.execute() } returns mockResponse

        // Act
        val result = dataSource.createCustomerTokenWithMultipass("multipass_secret")

        // Assert
        assertNull(result)
    }

    @Test
    fun `createCustomerTokenWithMultipass returns null when customerAccessToken is null`() = runTest {
        // Arrange
        val mockPayload = CustomerAccessTokenCreateWithMultipassMutation.CustomerAccessTokenCreateWithMultipass(
            customerAccessToken = null,
            customerUserErrors = emptyList(),
        )
        val mockData = CustomerAccessTokenCreateWithMultipassMutation.Data(
            customerAccessTokenCreateWithMultipass = mockPayload
        )
        val mockResponse = ApolloResponse.Builder(
            operation = CustomerAccessTokenCreateWithMultipassMutation(multipassToken = "multipass_secret"),
            requestUuid = uuid4(),
        ).data(mockData).build()

        val mockCall = mockk<ApolloCall<CustomerAccessTokenCreateWithMultipassMutation.Data>>()
        every { apolloClient.mutation(any<CustomerAccessTokenCreateWithMultipassMutation>()) } returns mockCall
        coEvery { mockCall.execute() } returns mockResponse

        // Act
        val result = dataSource.createCustomerTokenWithMultipass("multipass_secret")

        // Assert
        assertNull(result)
    }

    @Test
    fun `createCustomerTokenWithMultipass returns null when an exception is thrown`() = runTest {
        // Arrange
        every { apolloClient.mutation(any<CustomerAccessTokenCreateWithMultipassMutation>()) } throws Exception("Apollo error")

        // Act
        val result = dataSource.createCustomerTokenWithMultipass("multipass_secret")

        // Assert
        assertNull(result)
    }
}
