package com.dukkan.domain.repository

import com.dukkan.domain.model.Review
import kotlinx.coroutines.flow.SharedFlow

interface ReviewRepository {
    /**
     * Emits updates when a review is submitted.
     * The pair contains the product GID and a Review object with the new rating.
     */
    val reviewUpdates: SharedFlow<Pair<String, Review>>

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
    ): Result<String>
}
