package com.msayeh.domain.usecase.coupon


import javax.inject.Inject


data class CouponUseCases @Inject constructor(
    val getSavedCoupon: GetSavedCouponUseCase,
    val saveCoupon: SaveCouponUseCase,
    val clearCoupon: ClearCouponUseCase,
    val getAvailableCoupons: GetAvailableCouponsUseCase
)
