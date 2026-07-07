package com.dukkan.data.source.remote.location

import com.google.gson.annotations.SerializedName

data class LocationIQPredictionDto(
    @SerializedName("place_id") val placeId: String?,
    @SerializedName("lat") val lat: String?,
    @SerializedName("lon") val lon: String?,
    @SerializedName("display_name") val displayName: String?,
    @SerializedName("address") val address: LocationIQAddressDto?
)

data class LocationIQAddressDto(
    @SerializedName("name") val name: String?,
    @SerializedName("house_number") val houseNumber: String?,
    @SerializedName("road") val road: String?,
    @SerializedName("neighborhood") val neighborhood: String?,
    @SerializedName("suburb") val suburb: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("town") val town: String?,
    @SerializedName("village") val village: String?,
    @SerializedName("state") val state: String?,
    @SerializedName("postcode") val postcode: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("country_code") val countryCode: String?
)
