package com.dukkan.data.di

import com.apollographql.apollo.ApolloClient
import com.dukkan.data.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Singleton
    @Provides
    fun provideApolloClient(): ApolloClient = ApolloClient.Builder()
        .serverUrl("https://mad46-and5.myshopify.com/api/2026-04/graphql.json")
        .addHttpHeader(
            "X-Shopify-Storefront-Access-Token",
            BuildConfig.SHOPIFY_STOREFRONT_TOKEN
        )
        .build()
}