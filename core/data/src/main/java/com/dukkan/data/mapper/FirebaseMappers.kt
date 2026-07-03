package com.dukkan.data.mapper

import com.dukkan.data.source.remote.dto.ShopifyCustomerTokenDto
import com.dukkan.data.source.remote.dto.UserAuthDto
import com.google.firebase.auth.FirebaseUser
import com.dukkan.domain.model.AuthUser
import com.dukkan.domain.model.ShopifyToken

fun FirebaseUser?.toUserAuthDto(): UserAuthDto? {
    this ?: return null
    return UserAuthDto(uid = uid, email = email ?: "", name = displayName ?: "")
}

fun UserAuthDto.toDomainModel(): AuthUser {
    return AuthUser(
        uid = uid,
        email = email,
        name = name,
    )
}

fun ShopifyCustomerTokenDto.toDomainModel(): ShopifyToken =
    ShopifyToken(accessToken = accessToken, expiresAt = expiresAt)
