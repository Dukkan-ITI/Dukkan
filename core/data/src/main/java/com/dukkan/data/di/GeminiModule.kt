package com.dukkan.data.di

import com.dukkan.data.repository.GeminiRepositoryImpl
import com.dukkan.domain.repository.GeminiRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GeminiModule {

    @Binds
    @Singleton
    abstract fun bindGeminiRepository(
        impl: GeminiRepositoryImpl
    ): GeminiRepository
}
