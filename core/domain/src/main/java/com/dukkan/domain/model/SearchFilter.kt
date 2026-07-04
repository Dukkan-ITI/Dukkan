package com.dukkan.domain.model

data class SearchFilter(
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val availableOnly: Boolean = false,
    val vendors: List<String> = emptyList(),
    val productTypes: List<String> = emptyList()
) {
    val isActive: Boolean
        get() = minPrice != null || maxPrice != null ||
                availableOnly || vendors.isNotEmpty() || productTypes.isNotEmpty()
}
