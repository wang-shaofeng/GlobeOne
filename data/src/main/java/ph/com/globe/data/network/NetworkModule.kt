/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network

import android.content.Context
import android.os.Build
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import ph.com.globe.data.DataScope
import ph.com.globe.data.network_components.NetworkStatusProvider
import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.globeonesuperapp.data.BuildConfig
import ph.com.globe.model.util.CtaObjectArrayCustomAdapter
import ph.com.globe.model.util.JsonObjectToStringJsonAdapter
import ph.com.globe.model.util.brand.StringAsAccountBrandAdapter
import ph.com.globe.model.util.brand.StringAsAccountBrandTypeAdapter
import ph.com.globe.model.util.brand.StringAsAccountSegmentAdapter
import ph.com.globe.model.voucher.CouponStatusTypeAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Named

internal val sharedMoshi = Moshi.Builder()
    .add(JsonObjectToStringJsonAdapter())
    .add(StringAsAccountBrandAdapter())
    .add(StringAsAccountBrandTypeAdapter())
    .add(StringAsAccountSegmentAdapter())
    .add(CouponStatusTypeAdapter())
    .add(CtaObjectArrayCustomAdapter())
    .build()

@Module
internal object NetworkRetrofitModule {

    @Provides
    @DataScope
    fun provideConnectivityInterceptor(networkStatusProvider: NetworkStatusProvider): ConnectivityInterceptor =
        ConnectivityInterceptor(networkStatusProvider)

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
    @DataScope
    fun provideUserAgentInterceptor(context: Context): UserAgentInterceptor =
        UserAgentInterceptor(context.packageName)

    @Provides
    @Named(GLOBE_SERVER)
    @DataScope
    fun provideGlobeCertificatePinner(): CertificatePinner =
        CertificatePinner.Builder()
            .add(
                BuildConfig.GLOBE_SSL_PATTERN,
                "sha256/${BuildConfig.GLOBE_SSL_PIN}"
            )
            .build()

    @Provides
    @Named(LF_SERVER)
    @DataScope
    fun provideLfCertificatePinner(): CertificatePinner =
        CertificatePinner.Builder()
            .add(
                BuildConfig.LF_SSL_PATTERN,
                "sha256/${BuildConfig.LF_SSL_PIN}"
            )
            .build()

    @Provides
    @Named(OCS_SERVER)
    @DataScope
    fun provideOcsCertificatePinner(): CertificatePinner =
        CertificatePinner.Builder()
            .add(
                BuildConfig.OCS_SSL_PATTERN,
                "sha256/${BuildConfig.OCS_SSL_PIN}"
            )
            .build()

    @Provides
    @Named(RUSH_SERVER)
    @DataScope
    fun provideRushCertificatePinner(): CertificatePinner =
        CertificatePinner.Builder()
            .add(
                BuildConfig.RUSH_SSL_PATTERN,
                "sha256/${BuildConfig.RUSH_SSL_PIN}"
            )
            .build()

    @Provides
    @Named(GLOBE_SERVER)
    @DataScope
    fun provideGlobeOkHttpClient(
        context: Context,
        connectivityInterceptor: ConnectivityInterceptor,
        accessTokenInterceptor: AccessTokenInterceptor,
        userTokenInterceptor: UserTokenInterceptor,
        deviceIdInterceptor: DeviceIdInterceptor,
        userAgentInterceptor: UserAgentInterceptor,
        @Named(GLOBE_SERVER) certPinner: CertificatePinner
    ): OkHttpClient = OkHttpClient.Builder()
        .certificatePinner(certPinner)
        .addInterceptor(initChuckerInterceptor(context))
        .addInterceptor(accessTokenInterceptor)
        .addInterceptor(userTokenInterceptor)
        .addInterceptor(deviceIdInterceptor)
        .addInterceptor(userAgentInterceptor)
        .addInterceptor(connectivityInterceptor)
        .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addNetworkInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    @Provides
    @Named(LF_SERVER)
    @DataScope
    fun provideLfOkHttpClient(
        context: Context,
        userAgentInterceptor: UserAgentInterceptor,
        connectivityInterceptor: ConnectivityInterceptor,
        @Named(LF_SERVER) certPinner: CertificatePinner
    ): OkHttpClient = OkHttpClient.Builder()
        .certificatePinner(certPinner)
        .addInterceptor(initChuckerInterceptor(context))
        .addInterceptor(userAgentInterceptor)
        .addInterceptor(connectivityInterceptor)
        .also { setCypherSpecIfNeeded(it) }
        .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addNetworkInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    @Provides
    @Named(GLOBE_SIGN_IN_SERVER)
    @DataScope
    fun provideGlobeSignInOkHttpClient(
        context: Context,
        connectivityInterceptor: ConnectivityInterceptor,
        userAgentInterceptor: UserAgentInterceptor,
        deviceIdInterceptor: DeviceIdInterceptor,
        @Named(GLOBE_SERVER) certPinner: CertificatePinner
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(initChuckerInterceptor(context))
        .addInterceptor(deviceIdInterceptor)
        .addInterceptor(userAgentInterceptor)
        .addInterceptor(connectivityInterceptor)
        .certificatePinner(certPinner)
        .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addNetworkInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    @Provides
    @Named(OCS_SERVER)
    @DataScope
    fun provideOcsOkHttpClient(
        context: Context,
        userAgentInterceptor: UserAgentInterceptor,
        @Named(OCS_SERVER) certPinner: CertificatePinner
    ): OkHttpClient = OkHttpClient.Builder()
        .certificatePinner(certPinner)
        .addInterceptor(initChuckerInterceptor(context))
        .addInterceptor(userAgentInterceptor)
        .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addNetworkInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    @Provides
    @Named(RUSH_SERVER)
    @DataScope
    fun provideRushOkHttpClient(
        context: Context,
        userAgentInterceptor: UserAgentInterceptor,
        @Named(RUSH_SERVER) certPinner: CertificatePinner
    ): OkHttpClient = OkHttpClient.Builder()
        .certificatePinner(certPinner)
        .addInterceptor(initChuckerInterceptor(context))
        .addInterceptor(userAgentInterceptor)
        .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addNetworkInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    @Provides
    @Named(CMS_SERVER)
    @DataScope
    fun provideCmsOkHttpClient(
        context: Context,
        connectivityInterceptor: ConnectivityInterceptor,
        userAgentInterceptor: UserAgentInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(initChuckerInterceptor(context))
        .addInterceptor(connectivityInterceptor)
        .addInterceptor(userAgentInterceptor)
        .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addNetworkInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    @Provides
    @Named(G2_ENCRYPTION_SERVER)
    @DataScope
    fun provideCognitoOkHttpClient(
        context: Context,
        connectivityInterceptor: ConnectivityInterceptor,
        userAgentInterceptor: UserAgentInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(initChuckerInterceptor(context))
        .addInterceptor(connectivityInterceptor)
        .addInterceptor(userAgentInterceptor)
        .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addNetworkInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    @Provides
    @Named(GLOBE_SERVER)
    @DataScope
    fun provideGlobeRetrofit(@Named(GLOBE_SERVER) okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(NullOnEmptyConverterFactory)
            .addConverterFactory(MoshiConverterFactory.create(sharedMoshi))
            .baseUrl(BuildConfig.GLOBE_BASE_URL)
            .client(okHttpClient)
            .build()

    @Provides
    @Named(GLOBE_SIGN_IN_SERVER)
    @DataScope
    fun provideGlobeSignInRetrofit(@Named(GLOBE_SIGN_IN_SERVER) okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(NullOnEmptyConverterFactory)
            .addConverterFactory(MoshiConverterFactory.create(sharedMoshi))
            .baseUrl(BuildConfig.GLOBE_BASE_SIGN_IN_URL)
            .client(okHttpClient)
            .build()

    @Provides
    @Named(LF_SERVER)
    @DataScope
    fun provideLfRetrofit(@Named(LF_SERVER) okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(sharedMoshi))
            .baseUrl(BuildConfig.LF_BASE_URL)
            .client(okHttpClient)
            .build()

    @Provides
    @Named(OCS_SERVER)
    @DataScope
    fun provideOcsRetrofit(@Named(OCS_SERVER) okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(sharedMoshi))
            .baseUrl(BuildConfig.OCS_BASE_URL)
            .client(okHttpClient)
            .build()

    @Provides
    @Named(RUSH_SERVER)
    @DataScope
    fun providesRushRetrofit(@Named(RUSH_SERVER) okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(sharedMoshi))
            .baseUrl(BuildConfig.RUSH_BASE_URL)
            .client(okHttpClient)
            .build()

    @Provides
    @Named(CMS_SERVER)
    @DataScope
    fun provideCmsRetrofit(@Named(CMS_SERVER) okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(NullOnEmptyConverterFactory)
            .addConverterFactory(MoshiConverterFactory.create(sharedMoshi))
            .baseUrl(BuildConfig.CMS_BASE_URL)
            .client(okHttpClient)
            .build()

    @Provides
    @Named(G2_ENCRYPTION_SERVER)
    @DataScope
    fun provideCognitoRetrofit(@Named(G2_ENCRYPTION_SERVER) okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(NullOnEmptyConverterFactory)
            .addConverterFactory(MoshiConverterFactory.create(sharedMoshi))
            .baseUrl(BuildConfig.GLOBE_COGNITO_URL)
            .client(okHttpClient)
            .build()
}

fun setCypherSpecIfNeeded(builder: OkHttpClient.Builder) {
    // Android 7.0 has an issue with EC cypher suites
    // https://stackoverflow.com/q/39133437/8469719
    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
        val spec: ConnectionSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .cipherSuites(
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384
            )
            .build()
        builder.connectionSpecs(
            Collections
                .singletonList(spec)
        )
    }
}

internal object NullOnEmptyConverterFactory : Converter.Factory() {

    fun converterFactory() = this

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ) = object : Converter<ResponseBody, Any?> {
        val nextResponseBodyConverter =
            retrofit.nextResponseBodyConverter<Any?>(converterFactory(), type, annotations)

        override fun convert(value: ResponseBody) =
            if (value.contentLength() != 0L) nextResponseBodyConverter.convert(value) else null
    }
}

const val GLOBE_SERVER = "GlobeServer"
const val GLOBE_SIGN_IN_SERVER = "GlobeSignInServer"
const val LF_SERVER = "LFServer"
const val OCS_SERVER = "OCSServer"
const val RUSH_SERVER = "RushServer"
const val CMS_SERVER = "CMSServer"
const val G2_ENCRYPTION_SERVER = "CognitoServer"
