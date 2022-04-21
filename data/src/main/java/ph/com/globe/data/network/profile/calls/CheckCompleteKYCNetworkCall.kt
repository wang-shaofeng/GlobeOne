/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.profile.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.profile.ProfileRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.profile.CheckCompleteKYCError
import ph.com.globe.model.profile.response_models.CheckCompleteKYCResponse
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class CheckCompleteKYCNetworkCall @Inject constructor(
    private val profileRetrofit: ProfileRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(): LfResult<Boolean, CheckCompleteKYCError> {

        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(CheckCompleteKYCError.General(GeneralError.NotLoggedIn))
        }

        val response = kotlin.runCatching {
            profileRetrofit.checkCompleteKYC(headers)
        }.fold(Response<CheckCompleteKYCResponse>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                LfResult.success(it.result)
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific())
            }
        )
    }

    override val logTag = "GetAccountDetailsNetworkCall"
}

private fun NetworkError.toSpecific(): CheckCompleteKYCError {
    return CheckCompleteKYCError.General(GeneralError.Other(this))
}
