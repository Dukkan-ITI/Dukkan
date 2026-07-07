package com.dukkan.domain.usecase.auth

import com.dukkan.domain.model.AuthUser
import com.dukkan.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthUser> {
        return authRepository.login(email, password)
    }
}
