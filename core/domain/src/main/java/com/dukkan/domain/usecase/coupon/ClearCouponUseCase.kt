package com.dukkan.domain.usecase.coupon

import com.dukkan.domain.repository.CouponRepository
import javax.inject.Inject

class ClearCouponUseCase @Inject constructor(
    private val repository: CouponRepository
) {
    suspend operator fun invoke() {
        repository.clearCoupon()
    }
}
