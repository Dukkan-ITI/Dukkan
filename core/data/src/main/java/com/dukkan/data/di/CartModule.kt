package com.dukkan.data.di

import com.dukkan.data.repository.CartRepositoryImpl
import com.dukkan.data.source.local.data_source.cart.CartLocalDataSource
import com.dukkan.data.source.local.data_source.cart.CartLocalDataSourceImpl
import com.dukkan.data.source.remote.data_source.cart.CartRemoteDataSource
import com.dukkan.data.source.remote.data_source.cart.CartRemoteDataSourceImpl
import com.dukkan.data.source.remote.data_source.cart.CartFirestoreDataSource
import com.dukkan.data.source.remote.data_source.cart.CartFirestoreDataSourceImpl
import com.msayeh.domain.repository.CartRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CartModule {

    @Binds
    @Singleton
    abstract fun bindCartRepository(
        impl: CartRepositoryImpl
    ): CartRepository

    @Binds
    @Singleton
    abstract fun bindCartLocalDataSource(
        impl: CartLocalDataSourceImpl
    ): CartLocalDataSource

    @Binds
    @Singleton
    abstract fun bindCartRemoteDataSource(
        impl: CartRemoteDataSourceImpl
    ): CartRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindCartFirestoreDataSource(
        impl: CartFirestoreDataSourceImpl
    ): CartFirestoreDataSource
}
