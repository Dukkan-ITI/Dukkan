package com.dukkan.data.di

import com.dukkan.ai_agent.contract.AiTask
import com.dukkan.data.ai.handler.ChatbotTask
import com.dukkan.data.repository.ChatbotRepositoryImpl
import com.dukkan.domain.repository.ChatbotRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatbotModule {

    @Binds
    @Singleton
    abstract fun bindChatbotRepository(
        chatbotRepositoryImpl: ChatbotRepositoryImpl
    ): ChatbotRepository

    @Binds
    @IntoSet
    abstract fun bindChatbotTask(
        impl: ChatbotTask
    ): AiTask<*, *>
}