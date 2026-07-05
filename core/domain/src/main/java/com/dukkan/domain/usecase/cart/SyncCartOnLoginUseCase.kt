package com.dukkan.domain.usecase.cart


import com.dukkan.domain.repository.CartRepository
import javax.inject.Inject

class SyncCartOnLoginUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(userId: String) {
        cartRepository.syncCartOnLogin(userId)
    }
}