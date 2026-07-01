package com.dukkan.data.mapper

import com.dukkan.data.source.remote.dto.ShopifyCustomerTokenDto
import com.dukkan.data.source.remote.dto.UserAuthDto
import com.google.firebase.auth.FirebaseUser
import com.msayeh.domain.model.AuthUser
import com.msayeh.domain.model.ShopifyToken

fun FirebaseUser?.toUserAuthDto(): UserAuthDto? {
    return this?.let {
        UserAuthDto(
            uid = it.uid,
            email = it.email.orEmpty(),
            name = it.displayName.orEmpty()
        )
    }
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
