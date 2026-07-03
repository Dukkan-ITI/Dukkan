package com.dukkan.domain.usecase.cart

import com.dukkan.domain.model.CartItem
import com.dukkan.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow

import javax.inject.Inject

class GetCartItemsUseCase @Inject constructor(private val repository: CartRepository) {
    operator fun invoke(): Flow<List<CartItem>> {
        return repository.getAllCartItems()
    }
}
