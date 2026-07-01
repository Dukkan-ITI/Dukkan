package com.dukkan.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val imageUrl: String,
    val price: String,
    val currencyCode: String,
    val size: String,
    val quantity: Int
)
