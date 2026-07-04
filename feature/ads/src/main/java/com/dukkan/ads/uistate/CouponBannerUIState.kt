package com.dukkan.ads.uistate
import com.dukkan.domain.model.Coupon
data class CouponBannerUIState(
    val coupons: List<Coupon> = emptyList(),
    val savedSuccess: Boolean = false
)
