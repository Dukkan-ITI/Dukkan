package com.dukkan.domain.model

data class PredictiveSearchResult(
    val products: List<SearchProduct>,
    val collections: List<SearchCollection>
)
