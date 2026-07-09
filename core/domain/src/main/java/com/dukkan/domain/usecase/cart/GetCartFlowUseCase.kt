package com.dukkan.domain.usecase.cart

import com.dukkan.domain.model.cart.StoreCart
import com.dukkan.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCartFlowUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    operator fun invoke(): Flow<StoreCart?> {
        return cartRepository.getCartFlow()
    }
}
