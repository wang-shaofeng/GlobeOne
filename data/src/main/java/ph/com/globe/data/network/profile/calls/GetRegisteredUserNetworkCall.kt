/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.profile.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.profile.ProfileRetrofit
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.errors.GeneralError.Other
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.profile.GetRegisteredUserError
import ph.com.globe.model.profile.response_models.GetRegisteredUserResponse
import ph.com.globe.model.profile.response_models.GetRegisteredUserResponseResult
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class GetRegisteredUserNetworkCall @Inject constructor(
    private val profileRetrofit: ProfileRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(): LfResult<GetRegisteredUserResponseResult, GetRegisteredUserError> {

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
            profileRetrofit.getRegisteredUser(headers)
        }.fold(Response<GetRegisteredUserResponse>::toLfSdkResult, Throwable::toLFSdkResult)

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

    override val logTag = "GetRegisteredUserNetworkCall"
}

private fun NetworkError.toSpecific(): GetRegisteredUserError = GetRegisteredUserError.General(Other(this))
