package com.dukkan.domain.usecase.review

import com.dukkan.domain.repository.ReviewRepository
import javax.inject.Inject

class AddReviewUseCase @Inject constructor(
    private val repository: ReviewRepository,
) {
    suspend operator fun invoke(
        productGid: String,
        authorName: String,
        rating: Int,
        title: String,
        body: String,
    ): Result<String> = repository.submitReview(
        productGid = productGid,
        authorName = authorName,
        rating = rating,
        title = title,
        body = body,
    )
}
