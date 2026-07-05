package com.dukkan.domain.usecase

import com.dukkan.domain.repository.AuthRepository
import com.dukkan.domain.repository.OrderRepository
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
