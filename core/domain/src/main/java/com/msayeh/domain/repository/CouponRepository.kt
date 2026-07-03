package com.msayeh.domain.repository

import com.msayeh.domain.model.Coupon
import kotlinx.coroutines.flow.Flow

interface CouponRepository {
    val savedCoupon: Flow<String?>
    suspend fun saveCoupon(couponCode: String)
    suspend fun clearCoupon()
    fun getAvailableCoupons(): List<Coupon>
}
