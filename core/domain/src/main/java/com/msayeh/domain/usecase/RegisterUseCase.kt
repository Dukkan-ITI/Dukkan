package com.msayeh.domain.usecase

import com.msayeh.domain.model.AuthUser
import com.msayeh.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, firstName: String = "", lastName: String = ""): Result<AuthUser> {
        return authRepository.register(email, password, firstName, lastName)
    }
}
