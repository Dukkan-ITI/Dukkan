package com.dukkan.data.mapper

import com.dukkan.data.source.remote.dto.UserAuthDto
import com.dukkan.data.source.remote.dto.UserAuthDto
import com.google.firebase.auth.FirebaseUser
import com.msayeh.domain.model.AuthUser

fun FirebaseUser?.toUserAuthDto(): UserAuthDto? {
    return this?.let {
        UserAuthDto(
            uid = it.uid,
            email = it.email.orEmpty(),
            name = it.displayName.orEmpty()
        )
    }
}

fun UserAuthDto.toDomain(): AuthUser {
    return AuthUser(
        uid = uid,
        email = email,
        name = name,
    )
}

