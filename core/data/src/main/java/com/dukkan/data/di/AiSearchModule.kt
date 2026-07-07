package com.dukkan.data.di

import com.dukkan.data.repository.AiSearchRepositoryImpl
import com.dukkan.domain.repository.AiSearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiSearchModule {

    @Binds
    @Singleton
    abstract fun bindAiSearchRepository(
        impl: AiSearchRepositoryImpl
    ): AiSearchRepository
}
