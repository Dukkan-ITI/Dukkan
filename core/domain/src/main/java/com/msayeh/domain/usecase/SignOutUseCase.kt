package com.msayeh.domain.usecase

import com.msayeh.domain.repository.AuthRepository
import com.msayeh.domain.repository.OrderRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository,
) {
    suspend operator fun invoke() {
        authRepository.signOut()
        orderRepository.clearLocalOrders()
    }
}
