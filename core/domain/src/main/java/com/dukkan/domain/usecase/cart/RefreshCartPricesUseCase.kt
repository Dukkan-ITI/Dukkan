package com.dukkan.domain.usecase.cart

import com.dukkan.domain.model.AppCurrency
import com.dukkan.domain.model.CartItem
import com.dukkan.domain.repository.CartRepository
import com.dukkan.domain.repository.ProductsRepository

import javax.inject.Inject

class RefreshCartPricesUseCase @Inject constructor(
    private val productsRepository: ProductsRepository,
    private val cartRepository: CartRepository,
) {
    suspend operator fun invoke(items: List<CartItem>, targetCurrency: AppCurrency): Result<Unit> {
        val stale = items.filter { it.price.currencyCode != targetCurrency.code }
        if (stale.isEmpty()) return Result.success(Unit)
        return productsRepository.getVariantPrices(stale.map { it.id }).map { prices ->
            stale.forEach { item ->
                prices[item.id]?.let { newPrice -> cartRepository.updateCartItem(item.copy(price = newPrice)) }
            }
        }
    }
}
