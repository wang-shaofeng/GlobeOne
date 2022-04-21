/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.auth.calls

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
import ph.com.globe.model.auth.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class LoginEmailNetworkCall @Inject constructor(
    private val authRetrofit: AuthRetrofit,
    private val tokenRepository: TokenRepository,
    private val userDetailsRepository: UserDetailsRepository
) : HasLogTag {

    suspend fun execute(params: LoginEmailParams): LfResult<LoginResponse?, LoginError> {

        val headers = tokenRepository.createHeaderForAuth()

        val credentialsJson = Gson().toJson(params)
        val encryptedCredentials = encryptCredentials(credentialsJson)

        val body = LoginJsonRequest("traditional", encryptedCredentials, params.merge)

        val response = kotlin.runCatching {
            authRetrofit.login(headers, body)
        }.fold(Response<LoginResponse?>::toNullableLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                if (it != null) {
                    tokenRepository.setUserToken(it.result.userToken)
                    tokenRepository.setLoginStatus(LoginStatus.VERIFIED)
                } else {
                    return@fold LfResult.failure(LoginError.UserEmailNotVerified(params.email))
                }
                userDetailsRepository.setUserEmail(params.email)
                tokenRepository.setTimeWhenUserTokenWasFetched(System.currentTimeMillis())
                LfResult.success(it)
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific(params.email))
            }
        )
    }

    override val logTag = "LoginEmailNetworkCall"
}

private fun NetworkError.toSpecific(email: String): LoginError {

    when (this) {
        is NetworkError.Http -> {
            if (httpStatusCode == 400 && errorResponse?.error?.code == "40005" && errorResponse?.error?.details == "Incorrect username or password." || httpStatusCode == 404 && errorResponse?.error?.code == "40402" && (errorResponse?.error?.details == "The channel has no existing credentials." || errorResponse?.error?.details == "No user found.")) {
                return LoginError.InvalidUsernameOrPassword
            }
            if (httpStatusCode == 500 && errorResponse?.error?.code == "50202"
                && errorResponse?.error?.details == "Email hasn't verified yet."
            ) {
                return LoginError.UserEmailNotVerified(email)
            }
            if (httpStatusCode == 500 && errorResponse?.error?.code == "50202"
                && errorResponse?.error?.details == "Too many failed login attempts."
            ) {
                return LoginError.TooManyFailedLogins
            }
        }

        else -> Unit
    }
    return LoginError.General(Other(this))
}
