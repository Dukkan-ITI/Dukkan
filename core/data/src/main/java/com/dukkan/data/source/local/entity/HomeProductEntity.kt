package com.dukkan.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "home_products")
data class HomeProductEntity(
    @PrimaryKey val id: String,
    val title: String,
    val vendor: String,
    val imageUrl: String?,
    val priceAmount: String,
    val currencyCode: String,
    val rating: Float?,
    val reviewCount: Int?,
    val productType: String?,
    val description: String?,
    val imagesJson: String?, // For multi-image support if needed later
    val variantsJson: String? // For variants support if needed later
)
