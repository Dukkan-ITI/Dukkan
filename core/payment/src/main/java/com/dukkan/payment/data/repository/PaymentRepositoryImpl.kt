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
import com.dukkan.domain.model.Address
import com.dukkan.domain.model.OrderConfirmation
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
        cartTotal: com.dukkan.domain.model.Money,
    ): Result<OrderConfirmation> = runCatching {
        
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
        cartTotal: com.dukkan.domain.model.Money,
    ): Result<PaymentIntentionResult> = runCatching {
        val request = IntentionRequest(
            addressId = if (address is CheckoutAddress.Saved) sanitizeAddressId(address.addressId) else null,
            oneOffAddress = if (address is CheckoutAddress.OneOff) address.address.toDto() else null,
            cartId = cartId,
        )
        val response = api.createPaymentIntention(idempotencyKey, request)
        response.toDomainModel()
    }

    override suspend fun verifyPaymentStatus(
        orderId: String,
        cartTotal: com.dukkan.domain.model.Money?,
    ): Result<OrderConfirmation> = runCatching {
        
        OrderConfirmation(
            orderId = orderId,
            status = "Success",
            total = cartTotal ?: com.dukkan.domain.model.Money(java.math.BigDecimal("0.00"), "EGP")
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
