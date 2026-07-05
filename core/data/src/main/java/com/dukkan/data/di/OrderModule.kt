package com.dukkan.data.di

import com.apollographql.apollo.ApolloClient
import com.dukkan.data.repository.OrderRepositoryImpl
import com.dukkan.data.source.local.ShopifyTokenStore
import com.dukkan.data.source.remote.data_source.order.OrderRemoteDataSource
import com.dukkan.data.source.remote.data_source.order.OrderRemoteDataSourceImpl
import com.dukkan.domain.repository.OrderRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OrderModule {

    @Singleton
    @Provides
    fun provideOrderRemoteDataSource(apolloClient: ApolloClient): OrderRemoteDataSource =
        OrderRemoteDataSourceImpl(apolloClient)

    @Singleton
    @Provides
    fun provideOrderRepository(
        orderRemoteDataSource: OrderRemoteDataSource,
        tokenStore: ShopifyTokenStore,
        orderDao: com.dukkan.data.source.local.dao.OrderDao
    ): OrderRepository = OrderRepositoryImpl(orderRemoteDataSource, tokenStore, orderDao)
}
