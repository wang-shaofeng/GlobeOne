/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.auth.calls

import com.auth0.android.jwt.JWT
import com.google.gson.Gson
import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.auth.AuthRetrofit
import ph.com.globe.data.network.auth.model.LoginJsonRequest
import ph.com.globe.data.network.user_details.UserDetailsRepository
import ph.com.globe.data.network.util.*
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError.Other
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.auth.LoginError
import ph.com.globe.model.auth.LoginResponse
import ph.com.globe.model.auth.LoginSocialParams
import ph.com.globe.model.auth.LoginSocialResult
import ph.com.globe.model.auth.LoginStatus
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class LoginSocialNetworkCall @Inject constructor(
    private val authRetrofit: AuthRetrofit,
    private val tokenRepository: TokenRepository,
    private val userDetailsRepository: UserDetailsRepository
) : HasLogTag {

    suspend fun execute(params: LoginSocialParams): LfResult<LoginSocialResult, LoginError> {

        val headers = tokenRepository.createHeaderForAuth()

        var email: String? = null

        val credentialsJson = Gson().toJson(params)

        val encryptedCredentials = encryptCredentials(credentialsJson)

        val body = LoginJsonRequest("social", encryptedCredentials, params.merge)

        val response = kotlin.runCatching {
            authRetrofit.login(headers, body)
        }.fold(Response<LoginResponse?>::toNullableLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            { loginRes ->
                loginRes?.let { loginRes ->
                    logSuccessfulNetworkCall()
                    tokenRepository.setUserToken(loginRes.result.userToken)
                    tokenRepository.setLoginStatus(LoginStatus.VERIFIED)
                    tokenRepository.setTimeWhenUserTokenWasFetched(System.currentTimeMillis())

                    val decoded = JWT(loginRes.result.userToken)
                    email = decoded.claims["email"]?.asString()
                    val isNew = decoded.claims["isNew"]?.asBoolean()
                    val emailVerifiedDate =
                        decoded.claims["emailVerified"]?.asString()?.replace(" +", "+")

                    email?.let {
                        userDetailsRepository.setUserEmail(it)
                    }

                    LfResult.success(
                        LoginSocialResult.SocialLoginSuccessful(
                            loginRes.result.userToken,
                            email,
                            emailVerifiedDate,
                            isNew,
                        )
                    )
                } ?: run {
                    logFailedNetworkCall(NetworkError.InvalidParamsFormat)
                    LfResult.failure(NetworkError.InvalidParamsFormat.toSpecific())
                }
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific(email))
            }
        )
    }

    override val logTag = "LoginSocialNetworkCall"
}

private fun NetworkError.toSpecific(email: String? = null): LoginError {
    when (this) {
        is NetworkError.Http -> {
            // return email with the error so it can be used with social register api that follows
            if (httpStatusCode == 500 && errorResponse?.error?.code == "50202"
                && errorResponse?.error?.details?.contains("User Not Yet Registered.") == true
            ) {
                return LoginError.UserNotYetRegistered(email ?: "")
            }

            if (httpStatusCode == 500 && errorResponse?.error?.code == "50202"
                && errorResponse?.error?.details?.contains("email_address_in_use") == true
            ) {
                return LoginError.LoginWithThisEmailAlreadyExists(errorResponse?.moreInfo)
            }

            if (httpStatusCode == 400 && errorResponse?.error?.code == "40005"
                && errorResponse?.error?.details?.contains("Email is already in use") == true
            ) {
                return LoginError.LoginWithThisEmailAlreadyExists(
                    errorResponse?.moreInfo
                )
            }
        }

        else -> Unit
    }
    return LoginError.General(Other(this))
}
