package com.dukkan.domain.usecase

import com.dukkan.domain.model.ShopifyToken
import com.dukkan.domain.repository.AuthRepository
import javax.inject.Inject

class GetShopifyTokenUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): ShopifyToken? = authRepository.getShopifyToken()
}
