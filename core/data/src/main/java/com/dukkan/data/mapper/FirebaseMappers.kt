package com.dukkan.data.mapper

import com.dukkan.data.source.remote.dto.UserAuthDto
import com.google.firebase.auth.FirebaseUser

fun FirebaseUser?.toUserAuthDto(): UserAuthDto? {
    return this?.let {
        UserAuthDto(
            uid = it.uid,
            email = it.email.orEmpty(),
            name = it.displayName.orEmpty()
        )
    }
}

