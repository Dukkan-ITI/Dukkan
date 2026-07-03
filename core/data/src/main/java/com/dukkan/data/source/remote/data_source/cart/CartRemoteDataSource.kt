package com.dukkan.data.source.remote.data_source.cart

import com.dukkan.AddCartLinesMutation
import com.dukkan.CreateCartMutation
import com.dukkan.GetCartQuery
import com.dukkan.RemoveCartLinesMutation
import com.dukkan.UpdateCartLinesMutation

import com.dukkan.ApplyDiscountCodeMutation

interface CartRemoteDataSource {
    suspend fun createCart(customerAccessToken: String? = null): CreateCartMutation.CartCreate?
    suspend fun getCart(cartId: String): GetCartQuery.Cart?
    suspend fun addCartItem(cartId: String, variantId: String): AddCartLinesMutation.CartLinesAdd?
    suspend fun updateCartItem(cartId: String, lineId: String, quantity: Int): UpdateCartLinesMutation.CartLinesUpdate?
    suspend fun removeCartItem(cartId: String, lineId: String): RemoveCartLinesMutation.CartLinesRemove?


    suspend fun applyDiscountCodes(cartId: String, discountCodes: List<String>): ApplyDiscountCodeMutation.CartDiscountCodesUpdate?
}