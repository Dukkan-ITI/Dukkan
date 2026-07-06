package com.dukkan.domain.model

data class SearchIntent(
    val query: String,
    val category: String? = null,
    val color: String? = null,
    val size: String? = null,
    val maxPrice: Double? = null,
    val availableOnly: Boolean = false
)
