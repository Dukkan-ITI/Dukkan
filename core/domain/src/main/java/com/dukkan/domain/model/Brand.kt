package com.dukkan.domain.model

/**
 * Vendor field has no stable Shopify GID, so we use the vendor
 * string itself as the id — it's what we filter products by anyway.
 */
data class Brand(
    val id: String,
    val name: String
)