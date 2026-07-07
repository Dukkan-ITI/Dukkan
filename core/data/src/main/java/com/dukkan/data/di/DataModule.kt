package com.dukkan.data.di

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.dukkan.data.BuildConfig
import com.dukkan.data.repository.AddressRepositoryImpl
import com.dukkan.data.repository.AuthRepositoryImpl
import com.dukkan.data.repository.BrandsRepositoryImpl
import com.dukkan.data.repository.CouponRepositoryImpl
import com.dukkan.data.repository.PlacesRepositoryImpl
import com.dukkan.data.repository.ProductsRepositoryImpl
import com.dukkan.data.repository.SettingsRepositoryImpl
import com.dukkan.data.source.local.coupon.CouponStore
import com.dukkan.data.source.local.coupon.CouponStoreImpl
import com.dukkan.data.source.local.SettingsStore
import com.dukkan.data.source.local.SettingsStoreImpl
import com.dukkan.data.source.local.ShopifyTokenStore
import com.dukkan.data.source.local.ShopifyTokenStoreImpl
import com.dukkan.data.source.remote.data_source.auth.FirebaseAuthDataSource
import com.dukkan.data.source.remote.data_source.auth.FirebaseAuthDataSourceImpl
import com.dukkan.data.source.remote.data_source.auth.FirebaseStoreDataSourceImp
import com.dukkan.data.source.remote.data_source.auth.IFirebaseStoreDataSource
import com.dukkan.data.source.remote.data_source.auth.ShopifyAuthDataSource
import com.dukkan.data.source.remote.data_source.auth.ShopifyAuthDataSourceImpl
import com.dukkan.data.source.remote.apollo.AddressDataSource
import com.dukkan.data.source.remote.apollo.AddressDataSourceImpl
import com.dukkan.data.source.remote.apollo.ProductsDataSource
import com.dukkan.data.source.remote.apollo.ProductsDataSourceImpl
import com.dukkan.data.source.remote.location.LocationIQApiService
import com.dukkan.domain.repository.AddressRepository
import com.dukkan.domain.repository.AuthRepository
import com.dukkan.domain.repository.BrandsRepository
import com.dukkan.domain.repository.CouponRepository
import com.dukkan.domain.repository.PlacesRepository
import com.dukkan.domain.repository.ProductsRepository
import com.dukkan.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

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
    fun provideProductsRepository(
        productsDataSource: ProductsDataSource,
        settingsRepository: SettingsRepository,
    ): ProductsRepository =
        ProductsRepositoryImpl(productsDataSource, settingsRepository)

    @Singleton
    @Provides
    fun provideSettingsStore(@ApplicationContext context: Context): SettingsStore =
        SettingsStoreImpl(context)

    @Singleton
    @Provides
    fun provideSettingsRepository(settingsStore: SettingsStore): SettingsRepository =
        SettingsRepositoryImpl(settingsStore)

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
    fun provideFirebaseStoreDataSource(): IFirebaseStoreDataSource =
        FirebaseStoreDataSourceImp()

    @Singleton
    @Provides
    fun provideAuthRepository(
        authDataSource: FirebaseAuthDataSource,
        firebaseStoreDataSource: IFirebaseStoreDataSource,
        shopifyAuthDataSource: ShopifyAuthDataSource,
        shopifyTokenStore: ShopifyTokenStore,
        apolloClient: ApolloClient,
    ): AuthRepository = AuthRepositoryImpl(
        authDataSource,
        firebaseStoreDataSource,
        shopifyAuthDataSource,
        shopifyTokenStore,
        apolloClient
    )

    @Singleton
    @Provides
    fun provideAddressDataSource(apolloClient: ApolloClient): AddressDataSource =
        AddressDataSourceImpl(apolloClient)

    @Singleton
    @Provides
    fun provideAddressRepository(
        addressDataSource: AddressDataSource,
        tokenStore: ShopifyTokenStore,
    ): AddressRepository = AddressRepositoryImpl(addressDataSource, tokenStore)

    @Singleton
    @Provides
    fun provideCouponStore(@ApplicationContext context: Context): CouponStore =
        CouponStoreImpl(context)

    @Singleton
    @Provides
    fun provideCouponRepository(couponStore: CouponStore): CouponRepository =
        CouponRepositoryImpl(couponStore)

    @Singleton
    @Provides
    fun provideBrandsRepository(productsDataSource: ProductsDataSource): BrandsRepository =
        BrandsRepositoryImpl(productsDataSource)

    @Singleton
    @Provides
    fun provideLocationIQApiService(): LocationIQApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        return retrofit2.Retrofit.Builder()
            .baseUrl("https://api.locationiq.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LocationIQApiService::class.java)
    }

    @Singleton
    @Provides
    fun providePlacesRepository(apiService: LocationIQApiService): PlacesRepository =
        PlacesRepositoryImpl(apiService, BuildConfig.LOCATION_IQ_API_KEY)
}
