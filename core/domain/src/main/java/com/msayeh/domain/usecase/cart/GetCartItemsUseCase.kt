package com.msayeh.domain.usecase.cart

import com.msayeh.domain.model.CartItem
import com.msayeh.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow

import javax.inject.Inject

class GetCartItemsUseCase @Inject constructor(private val repository: CartRepository) {
    operator fun invoke(): Flow<List<CartItem>> {
        return repository.getAllCartItems()
    }
}
