package com.dukkan.payment.data.remote.dto

import com.google.gson.annotations.SerializedName

// ── Paymob Intention API Request ─────────────────────────────────────────

internal data class PaymobIntentionRequest(
    @SerializedName("amount")          val amount: Long,
    @SerializedName("currency")        val currency: String,
    @SerializedName("payment_methods") val paymentMethods: List<Int>,
    @SerializedName("items")           val items: List<PaymobItem>,
    @SerializedName("billing_data")    val billingData: PaymobBillingData,
)

internal data class PaymobItem(
    @SerializedName("name")        val name: String,
    @SerializedName("amount")      val amount: Long,
    @SerializedName("description") val description: String,
    @SerializedName("quantity")    val quantity: Int,
)

internal data class PaymobBillingData(
    @SerializedName("first_name")   val firstName: String,
    @SerializedName("last_name")    val lastName: String,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("email")        val email: String,
)

// ── Paymob Intention API Response ────────────────────────────────────────

internal data class PaymobIntentionResponse(
    @SerializedName("client_secret") val clientSecret: String,
    @SerializedName("public_key")    val publicKey: String?,  // nullable — not always in response
)
