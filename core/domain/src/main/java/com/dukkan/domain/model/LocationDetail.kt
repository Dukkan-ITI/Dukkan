package com.dukkan.domain.model

data class LocationDetail(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val city: String?,
    val state: String?,
    val country: String?,
    val postalCode: String?
)
