/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.rewards.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.rewards.RewardsRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.rewards.RedeemPointsError
import ph.com.globe.model.account.NumberType
import ph.com.globe.model.account.toNumberType
import ph.com.globe.model.rewards.RedeemPointsRequestModel
import ph.com.globe.model.rewards.RedeemPointsResponseModel
import ph.com.globe.model.rewards.RedeemPointsResult
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class RedeemPointsNetworkCall @Inject constructor(
    private val rewardsRetrofit: RewardsRetrofit,
    private val tokenRepository: TokenRepository,
) : HasLogTag {
    suspend fun execute(
        msisdn: String,
        merchantNumber: String,
        amount: Float
    ): LfResult<RedeemPointsResult, RedeemPointsError> {
        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(RedeemPointsError.General(GeneralError.NotLoggedIn))
        }

        val response = runCatching {
            val body = when (msisdn.toNumberType()) {
                NumberType.MobileNumber ->
                    RedeemPointsRequestModel(msisdn, null, merchantNumber, amount)
                else ->
                    RedeemPointsRequestModel(null, msisdn, merchantNumber, amount)
            }
            
            rewardsRetrofit.redeemRewardsPoints(headers, body)
        }.fold(Response<RedeemPointsResponseModel>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold({
            logSuccessfulNetworkCall()
            LfResult.success(it.result)
        }, {
            logFailedNetworkCall(it)
            LfResult.failure(it.toSpecific())
        })
    }

    override val logTag: String = "RedeemPointsNetworkCall"
}

private fun NetworkError.toSpecific(): RedeemPointsError {
    if (this is NetworkError.Http) {
        when (errorResponse?.error?.code) {
            "50202" -> return RedeemPointsError.InsufficientBalancePoints
        }
    }
    return RedeemPointsError.General(GeneralError.Other(this))
}
