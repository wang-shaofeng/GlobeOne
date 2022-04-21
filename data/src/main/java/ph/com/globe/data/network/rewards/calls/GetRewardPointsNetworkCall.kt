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
import ph.com.globe.errors.rewards.GetRewardPointsError
import ph.com.globe.model.account.toHeaderPair
import ph.com.globe.model.rewards.GetRewardPointsResponse
import ph.com.globe.model.util.brand.AccountSegment.Mobile
import ph.com.globe.model.util.brand.SEGMENT_KEY
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetRewardPointsNetworkCall @Inject constructor(
    private val rewardsRetrofit: RewardsRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(
        msisdn: String,
        segment: String
    ): LfResult<GetRewardPointsResponse, GetRewardPointsError> {

        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(GetRewardPointsError.General(GeneralError.NotLoggedIn))
        }

        val response = kotlin.runCatching {
            rewardsRetrofit.getRewardPoints(
                headers,
                mapOf(msisdn.toHeaderPair(), SEGMENT_KEY to Mobile.toString())
            )
        }.fold(Response<GetRewardPointsResponse>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                LfResult.success(it)
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific())
            }
        )
    }

    override val logTag = "GetRewardPointsNetworkCall"
}

private fun NetworkError.toSpecific(): GetRewardPointsError {
    if (this is NetworkError.Http &&
        this.errorResponse?.error?.code == "50202" &&
        this.errorResponse?.error?.details == "Subscriber account is not registered."
    ) {
        return GetRewardPointsError.SubscriberAccountIsNotRegistered
    }
    return GetRewardPointsError.General(GeneralError.Other(this))
}
