package com.dukkan.data.repository

import com.dukkan.data.source.local.CouponData
import com.dukkan.data.source.local.CouponStore
import com.dukkan.domain.model.Coupon
import com.dukkan.domain.repository.CouponRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CouponRepositoryImpl @Inject constructor(
    private val couponStore: CouponStore
) : CouponRepository {
    override val savedCoupon: Flow<String?> = couponStore.savedCoupon

    override suspend fun saveCoupon(couponCode: String) {
        couponStore.saveCoupon(couponCode)
    }

    override suspend fun clearCoupon() {
        couponStore.clearCoupon()
    }


        override fun getAvailableCoupons(): List<Coupon> {
            return CouponData.coupons
        }
    }

