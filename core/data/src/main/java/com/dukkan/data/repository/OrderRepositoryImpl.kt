package com.dukkan.data.repository

import com.dukkan.data.mapper.OrderMapper
import com.dukkan.data.source.local.ShopifyTokenStore
import com.dukkan.data.source.local.dao.OrderDao
import com.dukkan.data.source.local.entity.OrderEntity
import com.dukkan.data.source.remote.data_source.order.OrderRemoteDataSource
import com.dukkan.domain.model.orders.Order
import com.dukkan.domain.model.orders.OrdersPage
import com.dukkan.domain.repository.OrderRepository
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val orderRemoteDataSource: OrderRemoteDataSource,
    private val tokenStore: ShopifyTokenStore,
    private val orderDao: OrderDao
) : OrderRepository {

    override suspend fun getOrders(first: Int, after: String?): Result<OrdersPage> {
        val token = tokenStore.getToken()?.accessToken
            ?: return Result.failure(Exception("User not logged in: customer access token is missing."))

        val remoteResult = orderRemoteDataSource.getCustomerOrders(token, first, after)
        
        return if (remoteResult.isSuccess) {
            val customer = remoteResult.getOrThrow()
            val edges = customer.orders.edges
            val pageInfo = customer.orders.pageInfo

            val orders = edges.map { edge ->
                OrderMapper.map(edge.node)
            }

            // Cache to local database if we are fetching the first page
            if (after == null) {
                orderDao.clearOrders()
                orderDao.insertOrders(orders.map { OrderEntity.fromDomainModel(it) })
            }

            Result.success(
                OrdersPage(
                    orders = orders,
                    hasNextPage = pageInfo.hasNextPage,
                    endCursor = pageInfo.endCursor
                )
            )
        } else {
            // Fallback to local cache if network fails (only for the first page)
            if (after == null) {
                val cachedOrders = orderDao.getAllOrders().map { it.toDomainModel() }
                if (cachedOrders.isNotEmpty()) {
                    Result.success(
                        OrdersPage(
                            orders = cachedOrders,
                            hasNextPage = false,
                            endCursor = null
                        )
                    )
                } else {
                    Result.failure(remoteResult.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } else {
                Result.failure(remoteResult.exceptionOrNull() ?: Exception("Unknown error"))
            }
        }
    }

    override suspend fun getOrderById(orderId: String): Result<Order> {
        return try {
            val cachedOrder = orderDao.getOrderById(orderId)?.toDomainModel()
            if (cachedOrder != null) {
                Result.success(cachedOrder)
            } else {
                // Not found locally, try fetching latest and check
                getOrders(50, null).mapCatching { page ->
                    page.orders.find { it.id == orderId } ?: throw Exception("Order not found")
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearLocalOrders() {
        orderDao.clearOrders()
    }
}
