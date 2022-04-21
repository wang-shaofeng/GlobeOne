/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.account.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.account.AccountRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.account.GetPlanDetailsError
import ph.com.globe.model.account.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetBroadbandPlanDetailsNetworkCall @Inject constructor(
    private val accountRetrofit: AccountRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: GetPlanDetailsParams): LfResult<GetBroadbandPlanDetailsResult, GetPlanDetailsError> {

        val headers = tokenRepository.createHeaderWithReferenceId(params.referenceId).successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(GetPlanDetailsError.General(GeneralError.NotLoggedIn))
        }
        val response = kotlin.runCatching {
            accountRetrofit.getBroadbandPlanDetails(headers, params.toQueryMap())
        }.fold(Response<GetBroadbandPlanDetailsResponse>::toLfSdkResult, Throwable::toLFSdkResult)

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

    override val logTag = "GetBroadbandPlanDetailsNetworkCall"
}

private fun NetworkError.toSpecific(): GetPlanDetailsError =
    GetPlanDetailsError.General(GeneralError.Other(this))
