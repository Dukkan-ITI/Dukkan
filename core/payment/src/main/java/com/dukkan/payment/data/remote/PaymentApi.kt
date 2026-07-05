package com.dukkan.payment.data.remote

import com.dukkan.payment.data.remote.dto.PaymobIntentionRequest
import com.dukkan.payment.data.remote.dto.PaymobIntentionResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

internal interface PaymentApi {

    /** POST https://accept.paymob.com/v1/intention/ */
    @POST("v1/intention/")
    suspend fun createIntention(
        @Header("Authorization") auth: String,
        @Body body: PaymobIntentionRequest,
    ): PaymobIntentionResponse
}
