/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ph.com.globe.data.DataScope
import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.globeonesuperapp.data.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
internal object TestNetworkRetrofitModule {

    @Provides
    @DataScope
    fun provideAccessTokenInterceptor(authDataManager: AuthDataManager): AccessTokenInterceptor =
        AccessTokenInterceptor(authDataManager)

    @Provides
    @DataScope
    fun provideUserTokenInterceptor(authDataManager: AuthDataManager): UserTokenInterceptor =
        UserTokenInterceptor(authDataManager)


    @Provides
    @DataScope
    fun provideDeviceIdInterceptor(authDataManager: AuthDataManager): DeviceIdInterceptor =
        DeviceIdInterceptor(authDataManager)

    @Provides
    @Named(G2_ENCRYPTION_SERVER)
    @DataScope
    fun provideG2EncryptionRetrofitBuilder(): Retrofit.Builder {
        val client: OkHttpClient = OkHttpClient.Builder()
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
            .connectTimeout(35, TimeUnit.SECONDS)
            .readTimeout(35, TimeUnit.SECONDS)
            .writeTimeout(35, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return Retrofit.Builder()
            .addConverterFactory(NullOnEmptyConverterFactory)
            .addConverterFactory(MoshiConverterFactory.create(sharedMoshi))
            .baseUrl(BuildConfig.GLOBE_COGNITO_URL)
            .client(client)
    }
    
    @Provides
    @Named(GLOBE_SERVER)
    fun provideGlobeRetrofitBuilder(
        accessTokenInterceptor: AccessTokenInterceptor,
        userTokenInterceptor: UserTokenInterceptor,
        deviceIdInterceptor: DeviceIdInterceptor
    ): Retrofit.Builder {
        val client: OkHttpClient = OkHttpClient.Builder()
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
            .connectTimeout(35, TimeUnit.SECONDS)
            .readTimeout(35, TimeUnit.SECONDS)
            .writeTimeout(35, TimeUnit.SECONDS)
            .addInterceptor(accessTokenInterceptor)
            .addInterceptor(userTokenInterceptor)
            .addInterceptor(deviceIdInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return Retrofit.Builder()
            .addConverterFactory(NullOnEmptyConverterFactory)
            .addConverterFactory(MoshiConverterFactory.create(sharedMoshi))
            .baseUrl(BuildConfig.GLOBE_BASE_URL)
            .client(client)
    }

    @Provides
    @Named(GLOBE_SIGN_IN_SERVER)
    @DataScope
    fun provideGlobeSignInRetrofitBuilder(
        deviceIdInterceptor: DeviceIdInterceptor
    ): Retrofit.Builder {
        val client = OkHttpClient.Builder()
            .addInterceptor(deviceIdInterceptor)
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addNetworkInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return Retrofit.Builder()
            .addConverterFactory(NullOnEmptyConverterFactory)
            .addConverterFactory(MoshiConverterFactory.create(sharedMoshi))
            .baseUrl(BuildConfig.GLOBE_BASE_SIGN_IN_URL)
            .client(client)
    }

    @Provides
    @Named(LF_SERVER)
    fun provideLFRetrofitBuilder(): Retrofit.Builder {
        val client: OkHttpClient = OkHttpClient.Builder()
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(sharedMoshi))
            .baseUrl(BuildConfig.LF_BASE_URL)
            .client(client)
    }

    @Provides
    @Named(OCS_SERVER)
    @DataScope
    fun provideOcsRetrofitBuilder(): Retrofit.Builder {
        val client: OkHttpClient = OkHttpClient.Builder()
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(sharedMoshi))
            .baseUrl(BuildConfig.OCS_BASE_URL)
            .client(client)
    }

    @Provides
    @Named(RUSH_SERVER)
    @DataScope
    fun provideRushRetrofitBuilder(): Retrofit.Builder {
        val client: OkHttpClient = OkHttpClient.Builder()
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(sharedMoshi))
            .baseUrl(BuildConfig.RUSH_BASE_URL)
            .client(client)
    }

    @Provides
    @Named(CMS_SERVER)
    @DataScope
    fun provideCmsRetrofitBuilder(): Retrofit.Builder {
        val client: OkHttpClient = OkHttpClient.Builder()
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(sharedMoshi))
            .baseUrl(BuildConfig.CMS_BASE_URL)
            .client(client)
    }

    @Provides
    @Named(G2_ENCRYPTION_SERVER)
    fun provideConfiguredG2EncryptionRetrofit(@Named(G2_ENCRYPTION_SERVER) retrofitBuilder: Retrofit.Builder): Retrofit =
        retrofitBuilder.build()

    @Provides
    @Named(GLOBE_SIGN_IN_SERVER)
    fun provideConfiguredGlobeSignInRetrofit(@Named(GLOBE_SIGN_IN_SERVER) retrofitBuilder: Retrofit.Builder): Retrofit =
        retrofitBuilder.build()

    @Provides
    @Named(GLOBE_SERVER)
    fun provideConfiguredGlobeRetrofit(@Named(GLOBE_SERVER) retrofitBuilder: Retrofit.Builder): Retrofit =
        retrofitBuilder.build()

    @Provides
    @Named(LF_SERVER)
    fun provideConfiguredLFRetrofit(@Named(LF_SERVER) retrofitBuilder: Retrofit.Builder): Retrofit =
        retrofitBuilder.build()

    @Provides
    @Named(OCS_SERVER)
    @DataScope
    fun provideOcsRetrofit(@Named(OCS_SERVER) retrofitBuilder: Retrofit.Builder): Retrofit =
        retrofitBuilder.build()

    @Provides
    @Named(RUSH_SERVER)
    @DataScope
    fun providesRushRetrofit(@Named(RUSH_SERVER) retrofitBuilder: Retrofit.Builder): Retrofit =
        retrofitBuilder.build()

    @Provides
    @Named(CMS_SERVER)
    @DataScope
    fun provideCmsRetrofit(@Named(CMS_SERVER) retrofitBuilder: Retrofit.Builder): Retrofit =
        retrofitBuilder.build()
}
