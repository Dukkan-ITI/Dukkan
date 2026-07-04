package com.dukkan.data.di

import com.dukkan.data.repository.SearchRepositoryImpl
import com.dukkan.data.source.remote.apollo.SearchDataSource
import com.dukkan.data.source.remote.apollo.SearchDataSourceImpl
import com.dukkan.domain.repository.SearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SearchModule {

    @Binds
    @Singleton
    abstract fun bindSearchDataSource(
        impl: SearchDataSourceImpl
    ): SearchDataSource

    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        impl: SearchRepositoryImpl
    ): SearchRepository
}
