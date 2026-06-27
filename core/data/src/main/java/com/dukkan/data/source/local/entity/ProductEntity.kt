package com.dukkan.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
     val id: String,
     val title: String,
     val imageUrl: String,
     val price: String,
     val currencyCode: String
)