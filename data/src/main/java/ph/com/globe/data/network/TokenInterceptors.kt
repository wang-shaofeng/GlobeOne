/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network

import android.content.Context
import android.os.Build
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.Version
import ph.com.globe.data.network.util.USER_TOKEN
import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.errors.ErrorTokenResponse
import ph.com.globe.errors.ErrorTokenResponseJsonAdapter
import ph.com.globe.errors.auth.RefreshUserTokenError
import ph.com.globe.globeonesuperapp.data.BuildConfig
import ph.com.globe.model.auth.LoginStatus
import javax.net.ssl.HttpsURLConnection

fun initChuckerInterceptor(context: Context) =
    ChuckerInterceptor.Builder(context).build() as Interceptor

internal val errorTokenResponseJsonAdapter = ErrorTokenResponseJsonAdapter(sharedMoshi)

internal class AccessTokenInterceptor(private val authDataManager: AuthDataManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = getToken()
        val originalRequest = chain.request()
        val request = originalRequest.newBuilder().also {
            if (accessToken != null && originalRequest.headers().get("Authorization") == null) {
                it.addHeader("Authorization", "Bearer $accessToken")
            }
        }.build()

        var response = chain.proceed(request)

        if (!response.isSuccessful && response.code() == HttpsURLConnection.HTTP_UNAUTHORIZED) {
            val body = response.peekBody(Long.MAX_VALUE).string()

            val errorResponse =
                runCatching { errorTokenResponseJsonAdapter.fromJson(body) }.getOrNull() as? ErrorTokenResponse
            val errorCode = errorResponse?.error?.code
            val errorMessage = errorResponse?.message
            if (errorCode in AUTHORIZATION_HEADERS || errorMessage == AUTHORIZATION_HEADER_MESSAGE) { // access token is not valid
                synchronized(this) {
                    val token = getToken()
                    val authHeader = request.header("Authorization")

                    // If false, it means that token is refreshed, if true, refresh it
                    if (token == null || authHeader != null && authHeader.contains(token)) {
                        updateToken()
                    }
                }

                val token = getToken()

                val newRequest = request.newBuilder().also {
                    it.header("Authorization", "Bearer $token")
                }.build()

                response = chain.proceed(newRequest)
            }
        }

        return response
    }

    private fun getToken() = authDataManager.getAccessToken().value

    private fun updateToken() = authDataManager.fetchAccessToken().value?.also {
        authDataManager.setAccessToken(it)
    }

    companion object {
        // The authorization header is not valid as client's credentials.
        private const val AUTHORIZATION_HEADER_40101 = "40101"

        // The authorization header is not valid.
        private const val AUTHORIZATION_HEADER_40102 = "40102"

        // The authorization header is expired.
        private const val AUTHORIZATION_HEADER_40103 = "40103"

        // The authorization header is not approved or is revoked.
        private const val AUTHORIZATION_HEADER_40104 = "40104"

        private const val AUTHORIZATION_HEADER_40301 = "40301"

        private val AUTHORIZATION_HEADERS = listOf(
            AUTHORIZATION_HEADER_40101,
            AUTHORIZATION_HEADER_40102,
            AUTHORIZATION_HEADER_40103,
            AUTHORIZATION_HEADER_40104,
            AUTHORIZATION_HEADER_40301
        )

        private const val AUTHORIZATION_HEADER_MESSAGE = "Unauthorized"
    }
}

internal class UserTokenInterceptor(private val authDataManager: AuthDataManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val currentUserToken = request.header(USER_TOKEN)

        var response = chain.proceed(chain.request())

        if (!response.isSuccessful && response.code() == HttpsURLConnection.HTTP_UNAUTHORIZED && currentUserToken != null) {
            val body = response.peekBody(Long.MAX_VALUE).string()
            val errorResponse =
                runCatching { errorTokenResponseJsonAdapter.fromJson(body) }.getOrNull() as? ErrorTokenResponse
            val errorCode = errorResponse?.error?.code
            if (errorCode in AUTHORIZATION_HEADERS) {

                synchronized(this) {
                    val token = getToken()
                    val userTokenHeader = request.header(USER_TOKEN)

                    // If false, it means that token is refreshed, if true, refresh it
                    if (token == null || userTokenHeader != null && userTokenHeader.contains(token)) {
                        if (updateToken().error is RefreshUserTokenError.RefreshTokenFailed) {
                            logoutIfUserIsLoggedIn()
                            return response
                        }
                    }
                }

                val token = getToken()

                val newRequest = request.newBuilder().also {
                    it.header(USER_TOKEN, "Bearer $token")
                }.build()

                response = chain.proceed(newRequest)
            }
        }

        return response
    }

    private fun logoutIfUserIsLoggedIn() {
        if (authDataManager.getLoginStatus() != LoginStatus.NOT_LOGGED_IN) {
            authDataManager.removeUserData()
            authDataManager.sendLogoutEvent(true)
        }
    }

    private fun getToken() = authDataManager.getUserToken().value

    private fun updateToken() = authDataManager.refreshUserToken().also {
        it.value?.let {
            authDataManager.setUserToken(it)
            authDataManager.setTimeWhenUserTokenWasFetched(System.currentTimeMillis())
        }
    }

    companion object {
        // The User-Token header is not valid.
        private const val AUTHORIZATION_HEADER_40105 = "40105"

        // The User-Token header is expired.
        private const val AUTHORIZATION_HEADER_40106 = "40106"

        // Mismatched User-Token details | User-Token details not found.
        private const val AUTHORIZATION_HEADER_40109 = "40109"

        private val AUTHORIZATION_HEADERS = listOf(
            AUTHORIZATION_HEADER_40105,
            AUTHORIZATION_HEADER_40106,
            AUTHORIZATION_HEADER_40109
        )
    }
}

internal class DeviceIdInterceptor(private val authDataManager: AuthDataManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val request = originalRequest.newBuilder().also {
            it.addHeader("DeviceId", authDataManager.getDeviceId())
        }.build()
        return chain.proceed(request)
    }
}

internal class UserAgentInterceptor(packageName: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestWithUserAgent =
            chain.request().newBuilder().header(USER_AGENT_HEADER, userAgent).build()

        return chain.proceed(requestWithUserAgent)
    }

    private val userAgent: String =
        "GlobeOne/" + //hardcoding app name to avoid having to read it using context
                "${BuildConfig.VERSION_NAME} " +
                "(${packageName}; " +
                "build:${BuildConfig.VERSION_CODE} " +
                "Android SDK ${Build.VERSION.SDK_INT}) " +
                Version.userAgent()

    companion object {
        private const val USER_AGENT_HEADER = "User-Agent"
    }
}
