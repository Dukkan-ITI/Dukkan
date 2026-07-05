package com.dukkan.domain.usecase.cart


import com.dukkan.domain.repository.CartRepository
import javax.inject.Inject

class ClearCartOnLogoutUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke() {
        cartRepository.clearLocalCart()
    }
}