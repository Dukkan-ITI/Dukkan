package com.dukkan.data.di

import com.dukkan.data.repository.CartRepositoryImpl
import com.dukkan.data.source.local.data_source.cart.CartLocalDataSource
import com.dukkan.data.source.local.data_source.cart.CartLocalDataSourceImpl
import com.dukkan.domain.repository.CartRepository
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
}
