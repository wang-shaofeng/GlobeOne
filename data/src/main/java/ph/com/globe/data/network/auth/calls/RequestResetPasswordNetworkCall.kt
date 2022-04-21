/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.auth.calls

import com.google.gson.Gson
import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.auth.AuthRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError.Other
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.auth.RequestResetPasswordError
import ph.com.globe.model.auth.RequestResetPasswordParams
import ph.com.globe.model.auth.RequestResetPasswordRequest
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class RequestResetPasswordNetworkCall @Inject constructor(
    private val authRetrofit: AuthRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: RequestResetPasswordParams): LfResult<Unit, RequestResetPasswordError> {

        val headers = tokenRepository.createHeaderForAuth()

        val credentialsJson = Gson().toJson(params)
        val encryptedCredentials = encryptCredentials(credentialsJson)

        val body = RequestResetPasswordRequest(encryptedCredentials)

        val response = kotlin.runCatching {
            authRetrofit.requestResetPassword(headers, body)
        }.fold(Response<Unit?>::toEmptyLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                LfResult.success(Unit)
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific())
            }
        )
    }

    override val logTag = "RequestResetPasswordNetworkCall"
}

private fun NetworkError.toSpecific(): RequestResetPasswordError {
    when (this) {
        is NetworkError.Http -> {
            when {
                errorResponse?.error?.code == "40402" && errorResponse?.error?.details?.contains("Email address not found") == true ->
                    return RequestResetPasswordError.NoRegisteredUserWithThisEmail
                errorResponse?.error?.code == "50202" && errorResponse?.error?.details?.contains("Email is for Social Login Only") == true ->
                    return RequestResetPasswordError.EmailIsForSocialLogin
            }
        }
        else -> Unit
    }

    return RequestResetPasswordError.General(Other(this))
}
