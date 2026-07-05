package com.dukkan.domain.usecase.customer

import com.dukkan.domain.repository.AuthRepository
import javax.inject.Inject

class GetCustomerIdUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<String> = authRepository.getCustomerId()
}
