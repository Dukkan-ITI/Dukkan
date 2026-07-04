package com.dukkan.payment.data.repository

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
    ): Result<OrderConfirmation> = runCatching {
        api.confirmCashOrder(
            idempotencyKey = idempotencyKey,
            body = CashOrderRequest(
                addressId = sanitizeAddressId((address as? CheckoutAddress.Saved)?.addressId),
                oneOffAddress = (address as? CheckoutAddress.OneOff)?.address?.toDto(),
                cartId = cartId,
            ),
        ).toDomainModel()
    }

    override suspend fun createPaymentIntention(
        idempotencyKey: String,
        address: CheckoutAddress,
        cartId: String,
    ): Result<PaymentIntentionResult> = runCatching {
        api.createPaymentIntention(
            idempotencyKey = idempotencyKey,
            body = IntentionRequest(
                addressId = sanitizeAddressId((address as? CheckoutAddress.Saved)?.addressId),
                oneOffAddress = (address as? CheckoutAddress.OneOff)?.address?.toDto(),
                cartId = cartId,
            )
        ).toDomainModel()
    }

    override suspend fun verifyPaymentStatus(
        orderId: String,
    ): Result<OrderConfirmation> = runCatching {
        api.getOrderStatus(orderId = orderId).toDomainModel()
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
