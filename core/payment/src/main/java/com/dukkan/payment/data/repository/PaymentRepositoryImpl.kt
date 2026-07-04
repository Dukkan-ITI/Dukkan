package com.dukkan.payment.data.repository
import okhttp3.MediaType.Companion.toMediaType

import com.dukkan.payment.data.mapper.toDomainModel
import com.dukkan.payment.data.remote.PaymentApi
import com.dukkan.payment.data.remote.dto.AddressDto
import com.dukkan.payment.data.remote.dto.CashOrderRequest
import com.dukkan.payment.data.remote.dto.IntentionRequest
import com.dukkan.payment.domain.model.CheckoutAddress
import com.dukkan.payment.domain.model.PaymentIntentionResult
import com.dukkan.payment.domain.repository.PaymentRepository
import com.msayeh.domain.model.Address
import com.msayeh.domain.model.OrderConfirmation
import javax.inject.Inject

internal class PaymentRepositoryImpl @Inject constructor(
    private val api: PaymentApi,
) : PaymentRepository {

    private fun sanitizeAddressId(rawId: String?): String? =
        rawId?.substringBefore("?")

    override suspend fun confirmCashOrder(
        idempotencyKey: String,
        address: CheckoutAddress,
        cartId: String,
        cartTotal: com.msayeh.domain.model.Money,
    ): Result<OrderConfirmation> = runCatching {
        // Option B: Bypass local backend
        OrderConfirmation(
            orderId = "CASH-" + java.util.UUID.randomUUID().toString().take(8),
            status = "Success",
            total = cartTotal
        )
    }

    override suspend fun createPaymentIntention(
        idempotencyKey: String,
        address: CheckoutAddress,
        cartId: String,
        cartTotal: com.msayeh.domain.model.Money,
    ): Result<PaymentIntentionResult> = runCatching {
        // Option B: Bypass local backend and create intention directly with Paymob API
        val secretKey = com.dukkan.payment.BuildConfig.PAYMOB_CLIENT_SECRET
        val publicKey = com.dukkan.payment.BuildConfig.PAYMOB_PUBLIC_KEY
        
        val amountInCents = (cartTotal.amount * java.math.BigDecimal("100")).toInt()
        val json = org.json.JSONObject().apply {
            put("amount", amountInCents) 
            // For testing: Force EGP since our integration IDs (5766356, 5766720) are strictly EGP
            put("currency", "EGP")
            
            // Add the integration IDs here!
            val paymentMethods = org.json.JSONArray().apply {
                put(5766356) // VPC (Card) Integration ID
                put(5766720) // Cash Integration ID
            }
            put("payment_methods", paymentMethods)
            
            val billing = org.json.JSONObject().apply {
                put("first_name", "Test")
                put("last_name", "User")
                put("email", "test@dukkan.com")
                put("phone_number", "01000000000")
            }
            put("billing_data", billing)
        }
        
        val client = okhttp3.OkHttpClient()
        val request = okhttp3.Request.Builder()
            .url("https://accept.paymob.com/v1/intention/")
            .post(okhttp3.RequestBody.create("application/json".toMediaType(), json.toString()))
            .addHeader("Authorization", "Token $secretKey")
            .build()
            
        val response = kotlinx.coroutines.Dispatchers.IO.let {
            kotlinx.coroutines.withContext(it) {
                client.newCall(request).execute()
            }
        }
        
        if (!response.isSuccessful) {
            error("Paymob API error: ${response.code} ${response.body?.string()}")
        }
        
        val responseBody = response.body?.string() ?: ""
        val jsonResponse = org.json.JSONObject(responseBody)
        val clientSecret = jsonResponse.getString("client_secret")
        
        PaymentIntentionResult(
            orderId = "ONLINE-${java.util.UUID.randomUUID().toString().take(8)}",
            clientSecret = clientSecret,
            publicKey = publicKey
        )
    }

    override suspend fun verifyPaymentStatus(
        orderId: String,
        cartTotal: com.msayeh.domain.model.Money?,
    ): Result<OrderConfirmation> = runCatching {
        // Option B: Bypass local backend and assume success since SDK finished
        OrderConfirmation(
            orderId = orderId,
            status = "Success",
            total = cartTotal ?: com.msayeh.domain.model.Money(java.math.BigDecimal("0.00"), "EGP")
        )
    }

    private fun Address.toDto(): AddressDto = AddressDto(
        firstName = firstName,
        lastName = lastName,
        company = company,
        address1 = address1,
        address2 = address2,
        city = city,
        province = province,
        country = country,
        zip = zip,
        phone = phone,
    )
}
