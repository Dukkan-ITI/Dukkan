package com.dukkan.ai_agent.di

import com.dukkan.ai_agent.contract.AiProvider
import com.dukkan.ai_agent.provider.firebase.FirebaseAiProvider
import com.dukkan.ai_agent.provider.ollama.OllamaProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AiAgentModule {
    @Binds
    @IntoSet
    abstract fun bindOllamaProvider(impl: OllamaProvider): AiProvider

    @Binds
    @IntoSet
    abstract fun bindFirebaseProvider(impl: FirebaseAiProvider): AiProvider
}

