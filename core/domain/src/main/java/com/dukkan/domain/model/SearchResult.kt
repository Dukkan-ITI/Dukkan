package com.dukkan.domain.model

data class SearchResult(
    val products: List<SearchProduct>,
    val pageInfo: PageInfo,
    val totalCount: Int
)
