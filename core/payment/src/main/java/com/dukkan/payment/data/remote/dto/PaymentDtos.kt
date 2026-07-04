package com.dukkan.payment.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class AddressDto(
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?,
    @SerializedName("company") val company: String?,
    @SerializedName("address1") val address1: String?,
    @SerializedName("address2") val address2: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("province") val province: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("zip") val zip: String?,
    @SerializedName("phone") val phone: String?,
)

internal data class CashOrderRequest(
    @SerializedName("address_id") val addressId: String?,
    @SerializedName("one_off_address") val oneOffAddress: AddressDto?,
    @SerializedName("cart_id") val cartId: String,
)

internal data class IntentionRequest(
    @SerializedName("address_id") val addressId: String?,
    @SerializedName("one_off_address") val oneOffAddress: AddressDto?,
    @SerializedName("cart_id") val cartId: String,
)

internal data class IntentionResponseDto(
    @SerializedName("order_id") val orderId: String,
    @SerializedName("client_secret") val clientSecret: String,
    @SerializedName("public_key") val publicKey: String,
)

internal data class OrderConfirmationDto(
    @SerializedName("order_id") val orderId: String,
    @SerializedName("status") val status: String,
    @SerializedName("total") val total: Double,
    @SerializedName("currency") val currency: String = "EGP",
)
