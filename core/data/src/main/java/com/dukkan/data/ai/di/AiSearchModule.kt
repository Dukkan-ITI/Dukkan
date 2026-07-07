package com.dukkan.data.ai.di

import com.dukkan.ai_agent.contract.AiTask
import com.dukkan.data.ai.handler.SearchTask
import com.dukkan.data.ai.repository.AiSearchRepositoryImpl
import com.dukkan.domain.repository.AiSearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiSearchModule {
    @Binds
    @Singleton
    abstract fun bindAiSearchRepository(
        impl: AiSearchRepositoryImpl
    ): AiSearchRepository

    @Binds
    @IntoSet
    abstract fun bindSearchTask(
        impl: SearchTask
    ): AiTask<*, *>
}
