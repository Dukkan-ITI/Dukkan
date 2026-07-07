package com.dukkan.data.repository

import com.dukkan.data.source.remote.review.ReviewAdminDataSource
import com.dukkan.domain.repository.ReviewRepository
import javax.inject.Inject

class ReviewRepositoryImpl @Inject constructor(
    private val adminDataSource: ReviewAdminDataSource,
) : ReviewRepository {

    override suspend fun submitReview(
        productGid: String,
        authorName: String,
        rating: Int,
        title: String,
        body: String,
    ): Result<Unit> = adminDataSource.submitReview(
        productGid = productGid,
        authorName = authorName,
        rating = rating,
        title = title,
        body = body,
    )
}
