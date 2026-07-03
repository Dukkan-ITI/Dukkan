package com.dukkan.domain.usecase.coupon

import com.dukkan.domain.repository.CouponRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedCouponUseCase @Inject constructor(
    private val repository: CouponRepository
) {
    operator fun invoke(): Flow<String?> = repository.savedCoupon
}