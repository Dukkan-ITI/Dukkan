package com.msayeh.domain.usecase

import com.msayeh.domain.model.AuthUser
import com.msayeh.domain.repository.AuthRepository
import javax.inject.Inject

class LoginWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<AuthUser> {
        return authRepository.loginWithGoogle(idToken)
    }
}
