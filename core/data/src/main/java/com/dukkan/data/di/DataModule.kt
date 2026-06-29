package com.dukkan.data.di

import com.apollographql.apollo.ApolloClient
import com.dukkan.data.BuildConfig
import com.dukkan.data.repository.AuthRepositoryImpl
import com.dukkan.data.repository.ProductsRepositoryImpl
import com.dukkan.data.source.remote.FirebaseAuthDataSourceImp
import com.dukkan.data.source.remote.IFirebaseAuthDataSource
import com.dukkan.data.source.remote.apollo.ProductsDataSource
import com.dukkan.data.source.remote.apollo.ProductsDataSourceImpl
import com.dukkan.data.source.remote.apollo.ProductsDataSourceImpl
import com.msayeh.domain.repository.AuthRepository
import com.msayeh.domain.repository.ProductsRepository
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
    fun provideFirebaseAuthDataSource(): IFirebaseAuthDataSource =
        FirebaseAuthDataSourceImp()

    @Singleton
    @Provides
    fun provideAuthRepository(authDataSource: IFirebaseAuthDataSource): AuthRepository =
        AuthRepositoryImpl(authDataSource)
}