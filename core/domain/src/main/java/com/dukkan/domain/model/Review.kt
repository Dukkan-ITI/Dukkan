package com.dukkan.domain.model

data class Review(
    val id: String,
    val authorName: String,
    val rating: Int,          // 1–5
    val title: String,
    val body: String,
    val createdAt: String,    // ISO-8601 string
    val approved: Boolean,
)
