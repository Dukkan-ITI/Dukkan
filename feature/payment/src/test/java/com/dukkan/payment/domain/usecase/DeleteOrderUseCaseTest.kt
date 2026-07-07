package com.dukkan.payment.domain.usecase

import com.dukkan.payment.domain.repository.OrderRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DeleteOrderUseCaseTest {
    private val repository = mockk<OrderRepository>()
    private lateinit var useCase: DeleteOrderUseCase

    @Before
    fun setUp() {
        useCase = DeleteOrderUseCase(repository)
    }

    @Test
    fun `invoke calls repository deleteOrder`() = runTest {
        val result = Result.success(Unit)
        coEvery { repository.deleteOrder("ord_1") } returns result

        val actual = useCase("ord_1")

        assertEquals(result, actual)
        coVerify(exactly = 1) { repository.deleteOrder("ord_1") }
    }
}
