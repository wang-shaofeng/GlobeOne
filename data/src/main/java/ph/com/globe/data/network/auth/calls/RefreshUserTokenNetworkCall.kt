/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.auth.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.auth.AuthRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.auth.RefreshUserTokenError
import ph.com.globe.model.auth.RefreshUserTokenResponse
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class RefreshUserTokenNetworkCall @Inject constructor(
    private val authRetrofit: AuthRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {
    fun execute(): LfResult<String, RefreshUserTokenError> {
        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(RefreshUserTokenError.General(GeneralError.NotLoggedIn))
        }

        val response = runCatching {
            authRetrofit.refreshUserToken(headers).execute()
        }.fold(Response<RefreshUserTokenResponse>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                LfResult.success(it.result.userToken)
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific())
            }
        )

    }

    override val logTag: String = "RefreshUserTokenNetworkCall"
}

private fun NetworkError.toSpecific(): RefreshUserTokenError =
    if (this is NetworkError.Http && errorResponse?.error?.code in REFRESH_TOKEN_ERROR_CODES && errorResponse?.error?.details in REFRESH_TOKEN_ERROR_DETAILS)
        RefreshUserTokenError.RefreshTokenFailed
    else
        RefreshUserTokenError.General(GeneralError.Other(this))

private val REFRESH_TOKEN_ERROR_CODES = arrayListOf("40109", "40402", "50202")
private val REFRESH_TOKEN_ERROR_DETAILS = arrayListOf(
    "Mismatched User-Token details.",
    "User-Token details not found.",
    "The channel has no existing credentials.",
    "The token you passed was not valid."
)
