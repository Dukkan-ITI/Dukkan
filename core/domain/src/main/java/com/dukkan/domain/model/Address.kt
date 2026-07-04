package com.dukkan.domain.model

data class Address(
    val id: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val company: String? = null,
    val address1: String? = null,
    val address2: String? = null,
    val city: String? = null,
    val province: String? = null,
    val country: String? = null,
    val zip: String? = null,
    val phone: String? = null,
    val isDefault: Boolean = false,
)
