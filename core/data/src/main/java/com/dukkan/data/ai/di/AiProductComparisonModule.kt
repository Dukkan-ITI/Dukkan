package com.dukkan.data.ai.di

import com.dukkan.ai_agent.contract.AiTask
import com.dukkan.data.ai.handler.ProductComparisonTask
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class AiProductComparisonModule {
    @Binds
    @IntoSet
    abstract fun bindProductComparisonTask(
        impl: ProductComparisonTask
    ): AiTask<*, *>
}
