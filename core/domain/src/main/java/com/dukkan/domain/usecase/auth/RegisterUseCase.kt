package com.dukkan.domain.usecase.auth

import com.dukkan.domain.model.AuthUser
import com.dukkan.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        firstName: String = "",
        lastName: String = ""
    ): Result<AuthUser> {
        return authRepository.register(email, password, firstName, lastName)
    }
}
