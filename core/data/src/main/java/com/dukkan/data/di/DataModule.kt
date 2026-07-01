package com.dukkan.data.di

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.dukkan.data.BuildConfig
import com.dukkan.data.repository.AuthRepositoryImpl
import com.dukkan.data.repository.ProductsRepositoryImpl
import com.dukkan.data.source.local.ShopifyTokenStore
import com.dukkan.data.source.local.ShopifyTokenStoreImpl
import com.dukkan.data.source.remote.FirebaseAuthDataSource
import com.dukkan.data.source.remote.FirebaseAuthDataSourceImpl
import com.dukkan.data.source.remote.ShopifyAuthDataSource
import com.dukkan.data.source.remote.ShopifyAuthDataSourceImpl
import com.dukkan.data.source.remote.apollo.ProductsDataSource
import com.dukkan.data.source.remote.apollo.ProductsDataSourceImpl
import com.msayeh.domain.repository.AuthRepository
import com.msayeh.domain.repository.ProductsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Singleton
    @Provides
    fun provideProductsDataSource(apolloClient: ApolloClient): ProductsDataSource =
        ProductsDataSourceImpl(apolloClient)

    @Singleton
    @Provides
    fun provideProductsRepository(productsDataSource: ProductsDataSource): ProductsRepository =
        ProductsRepositoryImpl(productsDataSource)

    @Singleton
    @Provides
    fun provideFirebaseAuthDataSource(): FirebaseAuthDataSource =
        FirebaseAuthDataSourceImpl()

    @Singleton
    @Provides
    fun provideShopifyAuthDataSource(apolloClient: ApolloClient): ShopifyAuthDataSource =
        ShopifyAuthDataSourceImpl(apolloClient)

    @Singleton
    @Provides
    fun provideShopifyTokenStore(@ApplicationContext context: Context): ShopifyTokenStore =
        ShopifyTokenStoreImpl(context)

    @Singleton
    @Provides
    fun provideAuthRepository(
        authDataSource: FirebaseAuthDataSource,
        shopifyAuthDataSource: ShopifyAuthDataSource,
        shopifyTokenStore: ShopifyTokenStore,
    ): AuthRepository = AuthRepositoryImpl(authDataSource, shopifyAuthDataSource, shopifyTokenStore)
}