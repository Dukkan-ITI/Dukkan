package com.msayeh.domain.model

data class SearchResult(
    val products: List<SearchProduct>,
    val pageInfo: PageInfo,
    val totalCount: Int
)
