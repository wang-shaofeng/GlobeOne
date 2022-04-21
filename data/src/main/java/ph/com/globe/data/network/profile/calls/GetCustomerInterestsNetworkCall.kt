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
import ph.com.globe.data.network.util.createAuthenticatedHeader
import ph.com.globe.data.network.util.logSuccessfulNetworkCall
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.profile.GetCustomerInterestsError
import ph.com.globe.model.profile.response_models.GetCustomerInterestsResponse
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetCustomerInterestsNetworkCall @Inject constructor(
    private val profileRetrofit: ProfileRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(): LfResult<List<String>, GetCustomerInterestsError> {

        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(GetCustomerInterestsError.General(GeneralError.NotLoggedIn))
        }

        val response = kotlin.runCatching {
            profileRetrofit.getCustomerInterests(headers)
        }.fold(Response<GetCustomerInterestsResponse>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                LfResult.success(it.results)
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific())
            }
        )
    }

    override val logTag = "GetCustomerInterestsNetworkCall"
}

private fun NetworkError.toSpecific(): GetCustomerInterestsError {
    return GetCustomerInterestsError.General(GeneralError.Other(this))
}
