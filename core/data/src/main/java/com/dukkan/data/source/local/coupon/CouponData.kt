package com.dukkan.data.source.local.coupon

import com.dukkan.domain.model.Coupon

object CouponData {

    val coupons = listOf(
        Coupon(
            id = "1",
            code = "DU-4",
            title = "Don't Miss\nToday's Deal",
            description = "LIMITED OFFER"
        ),
        Coupon(
            id = "2",
            code = "DU-04",
            title = "Refresh\nYour Style",
            description = "NEW ARRIVALS"
        ),
        Coupon(
            id = "3",
            code = "DU-404",
            title = "Save Big\nThis Weekend",
            description = "SPECIAL SALE"
        )
    )
}