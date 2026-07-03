package com.msayeh.domain.usecase.coupon

import com.msayeh.domain.repository.CouponRepository
import javax.inject.Inject

class SaveCouponUseCase @Inject constructor(
    private val repository: CouponRepository
) {
    suspend operator fun invoke(couponCode: String) {
        repository.saveCoupon(couponCode)
    }
}