package com.dukkan.payment.di

import com.apollographql.apollo.ApolloClient
import com.dukkan.payment.BuildConfig
import com.dukkan.payment.data.remote.admin.AdminOrderDataSource
import com.dukkan.payment.data.remote.admin.AdminOrderDataSourceImpl
import com.dukkan.payment.data.remote.PaymentApi
import com.dukkan.payment.data.repository.OrderRepositoryImpl
import com.dukkan.payment.data.repository.PaymentRepositoryImpl
import com.dukkan.payment.domain.repository.OrderRepository
import com.dukkan.payment.domain.repository.PaymentRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class PaymentModule {

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(
        impl: PaymentRepositoryImpl,
    ): PaymentRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        impl: OrderRepositoryImpl,
    ): OrderRepository

    @Binds
    @Singleton
    abstract fun bindAdminOrderDataSource(
        impl: AdminOrderDataSourceImpl,
    ): AdminOrderDataSource

    companion object {

        @Provides
        @Singleton
        @Named("paymentOkHttp")
        fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()

        @Provides
        @Singleton
        fun providePaymentApi(
            @Named("paymentOkHttp") okHttpClient: OkHttpClient,
        ): PaymentApi = Retrofit.Builder()
            .baseUrl("https://accept.paymob.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PaymentApi::class.java)

        @Provides
        @Singleton
        @AdminApollo
        fun provideAdminApolloClient(): ApolloClient = ApolloClient.Builder()
            .serverUrl("https://mad46-and5.myshopify.com/admin/api/2024-10/graphql.json")
            .addHttpHeader("X-Shopify-Access-Token", BuildConfig.SHOPIFY_ADMIN_TOKEN)
            .build()
    }
}
