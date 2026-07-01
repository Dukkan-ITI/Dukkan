package com.msayeh.domain.usecase

import com.msayeh.domain.model.ShopifyToken
import com.msayeh.domain.repository.AuthRepository
import javax.inject.Inject

class GetShopifyTokenUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): ShopifyToken? = authRepository.getShopifyToken()
}
