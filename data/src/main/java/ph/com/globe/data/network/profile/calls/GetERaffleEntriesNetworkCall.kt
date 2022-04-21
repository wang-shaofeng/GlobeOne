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
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.profile.GetERaffleEntriesError
import ph.com.globe.model.profile.*
import ph.com.globe.model.profile.response_models.GetERaffleEntriesResponse
import ph.com.globe.model.profile.response_models.GetERaffleEntriesResult
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetERaffleEntriesNetworkCall @Inject constructor(
    private val profileRetrofit: ProfileRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(): LfResult<GetERaffleEntriesResult, GetERaffleEntriesError> {

        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(GetERaffleEntriesError.General(GeneralError.NotLoggedIn))
        }

        val response = kotlin.runCatching {
            profileRetrofit.getERaffleEntries(headers)
        }.fold(Response<GetERaffleEntriesResponse>::toLfSdkResult, Throwable::toLFSdkResult)

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

private fun NetworkError.toSpecific(): GetERaffleEntriesError {
    return GetERaffleEntriesError.General(GeneralError.Other(this))
}
