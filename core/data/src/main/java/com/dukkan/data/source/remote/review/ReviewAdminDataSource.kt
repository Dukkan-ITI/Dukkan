package com.dukkan.data.source.remote.review

interface ReviewAdminDataSource {
    suspend fun submitReview(
        productGid: String,
        authorName: String,
        rating: Int,
        title: String,
        body: String,
    ): Result<String>
}
