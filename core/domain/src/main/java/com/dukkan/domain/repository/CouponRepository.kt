package com.dukkan.domain.repository

import com.dukkan.domain.model.Coupon
import kotlinx.coroutines.flow.Flow

interface CouponRepository {
    val savedCoupon: Flow<String?>
    suspend fun saveCoupon(couponCode: String)
    suspend fun clearCoupon()
    fun getAvailableCoupons(): List<Coupon>
}