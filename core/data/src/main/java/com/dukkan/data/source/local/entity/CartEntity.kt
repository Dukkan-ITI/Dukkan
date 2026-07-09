package com.dukkan.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dukkan.domain.model.cart.StoreCart
import com.google.gson.Gson

@Entity(tableName = "cart")
data class CartEntity(
    @PrimaryKey val id: String,
    val cartJson: String,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toDomainModel(gson: Gson): StoreCart? {
        return try {
            gson.fromJson(cartJson, StoreCart::class.java)
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        fun fromDomainModel(cart: StoreCart, gson: Gson): CartEntity {
            return CartEntity(
                id = cart.id,
                cartJson = gson.toJson(cart)
            )
        }
    }
}
