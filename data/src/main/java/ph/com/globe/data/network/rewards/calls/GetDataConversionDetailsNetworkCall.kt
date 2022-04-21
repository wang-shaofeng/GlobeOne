/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.rewards.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.rewards.RewardsRetrofit
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.rewards.GetDataConversionDetailsError
import ph.com.globe.model.rewards.GetDataConversionDetailsResponse
import ph.com.globe.model.rewards.GetDataConversionDetailsResult
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetDataConversionDetailsNetworkCall @Inject constructor(
    private val rewardsRetrofit: RewardsRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(conversionId: String): LfResult<GetDataConversionDetailsResult, GetDataConversionDetailsError> {

        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(GetDataConversionDetailsError.General(GeneralError.NotLoggedIn))
        }

        val response = kotlin.runCatching {
            rewardsRetrofit.getDataConversionDetails(
                headers,
                conversionId
            )
        }.fold(Response<GetDataConversionDetailsResponse>::toLfSdkResult, Throwable::toLFSdkResult)

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

    override val logTag = "GetDataConversionDetailsNetworkCall"
}

private fun NetworkError.toSpecific(): GetDataConversionDetailsError {
    return GetDataConversionDetailsError.General(GeneralError.Other(this))
}
