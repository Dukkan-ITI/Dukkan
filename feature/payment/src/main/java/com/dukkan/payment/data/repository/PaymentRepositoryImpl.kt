package com.dukkan.payment.data.repository

import com.dukkan.payment.BuildConfig
import com.dukkan.payment.data.remote.PaymentApi
import com.dukkan.payment.data.remote.dto.PaymobBillingData
import com.dukkan.payment.data.remote.dto.PaymobIntentionRequest
import com.dukkan.payment.data.remote.dto.PaymobItem
import com.dukkan.payment.domain.model.PaymentIntentionResult
import com.dukkan.payment.domain.repository.PaymentRepository
import com.dukkan.domain.model.Address
import com.dukkan.domain.model.Money
import com.dukkan.domain.model.OrderConfirmation
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

internal class PaymentRepositoryImpl @Inject constructor(
    private val api: PaymentApi,
) : PaymentRepository {

    override suspend fun confirmCashOrder(
        idempotencyKey: String,
        billingAddress: Address,
        cartId: String,
        cartTotal: Money,
    ): Result<OrderConfirmation> = runCatching {
        OrderConfirmation(
            orderId = "CASH-" + UUID.randomUUID().toString().take(8),
            status  = "Success",
            total   = cartTotal,
        )
    }

    override suspend fun createPaymentIntention(
        idempotencyKey: String,
        billingAddress: Address,
        cartId: String,
        cartTotal: Money,
    ): Result<PaymentIntentionResult> = runCatching {
        val amountInPiasters = cartTotal.amount
            .multiply(BigDecimal("100"))
            .toLong()

        val billing = billingAddress.toBillingData()

        val request = PaymobIntentionRequest(
            amount         = amountInPiasters,
            currency       = "EGP",
            paymentMethods = listOf(BuildConfig.PAYMOB_INTEGRATION_ID),
            items          = listOf(
                PaymobItem(
                    name        = "Dukkan Order",
                    amount      = amountInPiasters,
                    description = "Cart: $cartId",
                    quantity    = 1,
                )
            ),
            billingData = billing,
        )

        val response = api.createIntention(
            auth = "Token ${BuildConfig.PAYMOB_SECRET_KEY}",
            body = request,
        )

        PaymentIntentionResult(
            clientSecret = response.clientSecret,
            publicKey    = response.publicKey ?: BuildConfig.PAYMOB_PUBLIC_KEY,
            orderId      = cartId,
        )
    }

    override suspend fun verifyPaymentStatus(
        orderId: String,
        cartTotal: Money?,
    ): Result<OrderConfirmation> = runCatching {
        OrderConfirmation(
            orderId = orderId,
            status  = "Success",
            total   = cartTotal ?: Money(BigDecimal.ZERO, "EGP"),
        )
    }


    private fun Address.toBillingData() = PaymobBillingData(
        firstName   = firstName.orEmpty().ifEmpty { "Customer" },
        lastName    = lastName.orEmpty().ifEmpty { "." },
        phoneNumber = phone.orEmpty().ifEmpty { "N/A" },
        email       = "customer@example.com",
    )
}
