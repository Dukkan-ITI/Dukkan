package com.dukkan.domain.usecase.coupon

import com.dukkan.domain.repository.CouponRepository
import javax.inject.Inject

class SaveCouponUseCase @Inject constructor(
    private val repository: CouponRepository
) {
    suspend operator fun invoke(couponCode: String) {
        repository.saveCoupon(couponCode)
    }
}
