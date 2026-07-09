package com.dukkan.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "home_categories")
data class HomeCategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val handle: String
)
