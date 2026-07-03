package com.dukkan.domain.usecase.auth

import com.dukkan.domain.model.AuthUser
import com.dukkan.domain.repository.AuthRepository
import javax.inject.Inject

class LoginWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<AuthUser> {
        return authRepository.loginWithGoogle(idToken)
    }
}
