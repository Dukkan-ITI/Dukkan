package com.msayeh.domain.usecase.cart

import com.msayeh.domain.model.cart.StoreCart
import com.msayeh.domain.repository.CartRepository

import javax.inject.Inject

class GetCartUseCase @Inject constructor(private val repository: CartRepository) {
    suspend operator fun invoke(): StoreCart? {
        return repository.getCart()
    }
}
