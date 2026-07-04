package com.dukkan.ads.mapper

import com.dukkan.ads.R
import com.dukkan.domain.model.Coupon

fun Coupon.toImageResId(): Int = when (code) {
    "DU-4"   -> R.drawable.banner1
    "DU-40"  -> R.drawable.banner2
    "DU-404" -> R.drawable.banner3
    else     -> R.drawable.banner1
}
