package com.msayeh.domain.model

data class PredictiveSearchResult(
    val products: List<SearchProduct>,
    val collections: List<SearchCollection>
)
