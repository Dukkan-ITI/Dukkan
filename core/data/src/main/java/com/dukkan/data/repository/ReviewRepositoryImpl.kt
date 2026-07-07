package com.dukkan.data.repository

import com.dukkan.data.source.remote.review.ReviewAdminDataSource
import com.dukkan.domain.model.Review
import com.dukkan.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

class ReviewRepositoryImpl @Inject constructor(
    private val adminDataSource: ReviewAdminDataSource,
) : ReviewRepository {

    private val _reviewUpdates = MutableSharedFlow<Pair<String, Review>>(extraBufferCapacity = 1)
    override val reviewUpdates: SharedFlow<Pair<String, Review>> = _reviewUpdates.asSharedFlow()

    override suspend fun submitReview(
        productGid: String,
        authorName: String,
        rating: Int,
        title: String,
        body: String,
    ): Result<String> = adminDataSource.submitReview(
        productGid = productGid,
        authorName = authorName,
        rating = rating,
        title = title,
        body = body,
    ).onSuccess { reviewId ->
        _reviewUpdates.tryEmit(
            productGid to Review(
                id = reviewId,
                authorName = authorName,
                rating = rating,
                title = title,
                body = body,
                createdAt = "",
                approved = true
            )
        )
    }
}
