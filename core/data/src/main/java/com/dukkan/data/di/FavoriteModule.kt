package com.dukkan.data.di


import com.dukkan.data.repository.FavoriteRepositoryImpl
import com.dukkan.data.source.local.data_source.FavoriteLocalDataSource
import com.dukkan.data.source.local.data_source.FavoriteLocalDataSourceImpl

import com.msayeh.domain.repository.FavoriteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FavoriteModule {

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(
        impl: FavoriteRepositoryImpl
    ): FavoriteRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteLocalDataSource(
        impl: FavoriteLocalDataSourceImpl
    ): FavoriteLocalDataSource
}