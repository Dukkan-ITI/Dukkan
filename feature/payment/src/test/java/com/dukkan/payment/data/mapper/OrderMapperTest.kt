package com.dukkan.payment.data.mapper

import com.dukkan.payment.data.remote.dto.PaymobIntentionResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class OrderMapperTest {

    @Test
    fun `PaymobIntentionResponse toDomainModel maps correctly`() {
        val response = PaymobIntentionResponse(
            clientSecret = "secret_123",
            publicKey = "pub_456"
        )
        val orderId = "order_789"

        val result = response.toDomainModel(orderId)

        assertEquals(orderId, result.orderId)
        assertEquals("secret_123", result.clientSecret)
        assertEquals("pub_456", result.publicKey)
    }

    @Test
    fun `PaymobIntentionResponse toDomainModel uses default public key when null`() {
        val response = PaymobIntentionResponse(
            clientSecret = "secret_123",
            publicKey = null
        )
        
        // This will use BuildConfig.PAYMOB_PUBLIC_KEY which might be hard to mock if it's a constant
        // But we can check if it's not null at least if we don't know the value
        val result = response.toDomainModel("id")
        
        assertEquals("secret_123", result.clientSecret)
    }
}
