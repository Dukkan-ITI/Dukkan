package com.dukkan.payment.data.remote

import com.dukkan.payment.data.remote.dto.CashOrderRequest
import com.dukkan.payment.data.remote.dto.IntentionRequest
import com.dukkan.payment.data.remote.dto.IntentionResponseDto
import com.dukkan.payment.data.remote.dto.OrderConfirmationDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

internal interface PaymentApi {

    @POST("orders/cash-confirm")
    suspend fun confirmCashOrder(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: CashOrderRequest,
    ): OrderConfirmationDto

    @POST("orders/payment-intention")
    suspend fun createPaymentIntention(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: IntentionRequest,
    ): IntentionResponseDto

    @GET("orders/{orderId}/status")
    suspend fun getOrderStatus(
        @Path("orderId") orderId: String,
    ): OrderConfirmationDto
}
