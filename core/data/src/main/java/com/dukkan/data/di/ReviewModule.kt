package com.dukkan.data.di

import com.dukkan.data.BuildConfig
import com.dukkan.data.repository.ReviewRepositoryImpl
import com.dukkan.data.source.remote.review.ReviewAdminDataSource
import com.dukkan.data.source.remote.review.ReviewAdminDataSourceImpl
import com.dukkan.domain.repository.ReviewRepository
import com.apollographql.apollo.ApolloClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AdminApi

@Module
@InstallIn(SingletonComponent::class)
abstract class ReviewModule {

    @Binds
    @Singleton
    abstract fun bindReviewAdminDataSource(
        impl: ReviewAdminDataSourceImpl,
    ): ReviewAdminDataSource

    @Binds
    @Singleton
    abstract fun bindReviewRepository(
        impl: ReviewRepositoryImpl,
    ): ReviewRepository

    companion object {
        @Provides
        @Singleton
        @AdminApi
        fun provideAdminApolloClient(): ApolloClient = ApolloClient.Builder()
            .serverUrl("https://mad46-and5.myshopify.com/admin/api/2026-04/graphql.json")
            .addHttpHeader("X-Shopify-Access-Token", BuildConfig.SHOPIFY_ADMIN_TOKEN)
            .build()
    }
}
