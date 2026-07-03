package com.dukkan.domain.usecase.cart

import com.dukkan.domain.model.cart.StoreCart
import com.dukkan.domain.repository.CartRepository

import javax.inject.Inject

class GetCartUseCase @Inject constructor(private val repository: CartRepository) {
    suspend operator fun invoke(): StoreCart? {
        return repository.getCart()
    }
}
