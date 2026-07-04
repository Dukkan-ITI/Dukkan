package com.dukkan.domain.model

data class PageInfo(
    val hasNextPage: Boolean,
    val endCursor: String?
)
