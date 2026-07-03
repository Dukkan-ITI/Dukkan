package com.msayeh.domain.usecase.coupon

import com.msayeh.domain.repository.CouponRepository
import javax.inject.Inject

class ClearCouponUseCase @Inject constructor(
    private val repository: CouponRepository
) {
    suspend operator fun invoke() {
        repository.clearCoupon()
    }
}