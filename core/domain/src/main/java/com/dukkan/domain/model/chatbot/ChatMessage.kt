package com.dukkan.domain.model.chatbot
import com.dukkan.domain.model.SearchProduct
import com.dukkan.domain.model.orders.Order
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val products: List<SearchProduct> = emptyList(),
    val orders: List<Order> = emptyList()
)