package com.dukkan.domain.usecase.coupon

import com.dukkan.domain.model.Coupon
import com.dukkan.domain.repository.CouponRepository
import javax.inject.Inject

class GetAvailableCouponsUseCase @Inject constructor(
    private val repository: CouponRepository
) {
    operator fun invoke(): List<Coupon> = repository.getAvailableCoupons()
}