package com.dukkan.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
     val id: String,
     val title: String,
     val imageUrl: String,
     val price: String,
     val currencyCode: String
)
