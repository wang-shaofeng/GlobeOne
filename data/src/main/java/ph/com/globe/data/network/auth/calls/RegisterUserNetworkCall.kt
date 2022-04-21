/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.auth.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.auth.AuthRetrofit
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toEmptyLfSdkResult
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.auth.LoginError
import ph.com.globe.model.profile.response_models.RegisterUserParams
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class RegisterUserNetworkCall @Inject constructor(
    private val authRetrofit: AuthRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: RegisterUserParams): LfResult<Unit, LoginError> {
        val headers = tokenRepository.createAuthenticatedHeader().fold(
            {
                it
            },
            {
                logFailedToFetchAccessToken()
                return LfResult.failure(NetworkError.UserNotLoggedInError.toSpecific())
            }
        )

        val response = kotlin.runCatching {
            authRetrofit.registerUser(headers, params)
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

    override val logTag = "RegisterUserNetworkCall"
}

private fun NetworkError.toSpecific(): LoginError = LoginError.General(GeneralError.Other(this))
