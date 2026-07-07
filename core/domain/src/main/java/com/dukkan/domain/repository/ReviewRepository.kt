package com.dukkan.domain.repository

interface ReviewRepository {
    /**
     * Submits a new product review via the Shopify Admin API.
     * The 4-step flow (create metaobject, read existing list, append, set metafield)
     * is handled entirely in the data layer.
     */
    suspend fun submitReview(
        productGid: String,
        authorName: String,
        rating: Int,
        title: String,
        body: String,
    ): Result<Unit>
}
