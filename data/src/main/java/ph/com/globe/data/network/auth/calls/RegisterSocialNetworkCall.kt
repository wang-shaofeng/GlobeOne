/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.auth.calls

import com.google.gson.Gson
import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.auth.AuthRetrofit
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.auth.RegisterError
import ph.com.globe.model.auth.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class RegisterSocialNetworkCall @Inject constructor(
    private val authRetrofit: AuthRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: RegisterSocialParams): LfResult<RegisterSocialResult, RegisterError> {

        val headers = tokenRepository.createHeaderForAuth()

        val credentialsJson = Gson().toJson(params)
        val encryptedCredentials = encryptCredentials(credentialsJson)

        val body = RegisterSocialRequest("social", encryptedCredentials)

        val response = kotlin.runCatching {
            authRetrofit.registerSocial(headers, body)
        }.fold(Response<RegisterSocialResponse>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                tokenRepository.setUserToken(it.result)
                tokenRepository.setLoginStatus(LoginStatus.VERIFIED)
                tokenRepository.setTimeWhenUserTokenWasFetched(System.currentTimeMillis())
                LfResult.success(RegisterSocialResult.RegisterSuccessful(it.result))
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific())
            }
        )
    }

    override val logTag = "RegisterSocialNetworkCall"
}

private fun NetworkError.toSpecific(): RegisterError {

    when (this) {
        is NetworkError.Http -> {
            if (httpStatusCode == 500 && errorResponse?.error?.code == "50202" && errorResponse?.error?.details == "Email address is already in use.") {
                return RegisterError.EmailAddressAlreadyInUse
            }
            if (httpStatusCode == 400 && errorResponse?.error?.code == "40005" && (errorResponse?.error?.details == "Email address cannot be more than 128 characters." || errorResponse?.error?.details == "Password is not formatted correctly.")) {
                return RegisterError.InvalidParameters
            }
        }

        else -> Unit
    }
    return RegisterError.General(GeneralError.Other(this))
}
