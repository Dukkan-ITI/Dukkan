package com.msayeh.domain.usecase.coupon

import com.msayeh.domain.model.Coupon
import com.msayeh.domain.repository.CouponRepository
import javax.inject.Inject

class GetAvailableCouponsUseCase @Inject constructor(
    private val repository: CouponRepository
) {
    operator fun invoke(): List<Coupon> = repository.getAvailableCoupons()
}