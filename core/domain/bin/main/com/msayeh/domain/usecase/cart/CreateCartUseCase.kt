package com.msayeh.domain.usecase.cart

import com.msayeh.domain.repository.CartRepository
import javax.inject.Inject

class CreateCartUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(customerAccessToken: String? = null): String? {
        return repository.createCart(customerAccessToken)
    }
}
