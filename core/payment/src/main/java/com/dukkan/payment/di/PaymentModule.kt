package com.dukkan.payment.di

import com.dukkan.payment.data.remote.PaymentApi
import com.dukkan.payment.data.repository.PaymentRepositoryImpl
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

import com.dukkan.payment.BuildConfig

@Module
@InstallIn(SingletonComponent::class)
internal abstract class PaymentModule {

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(
        impl: PaymentRepositoryImpl,
    ): PaymentRepository

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
            .baseUrl(BuildConfig.PAYMENT_BACKEND_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PaymentApi::class.java)
    }
}
