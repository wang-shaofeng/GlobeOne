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
import ph.com.globe.errors.rewards.RedeemLoyaltyRewardsError
import ph.com.globe.model.account.toHeaderPair
import ph.com.globe.model.rewards.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class RedeemLoyaltyRewardsNetworkCall @Inject constructor(
    private val rewardsRetrofit: RewardsRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(
        msisdn: String,
        rewardsCatalogItem: RewardsCatalogItem,
        loyaltyProgramId: LoyaltyProgramId
    ): LfResult<RedeemLoyaltyRewardsResult, RedeemLoyaltyRewardsError> {

        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(RedeemLoyaltyRewardsError.General(GeneralError.NotLoggedIn))
        }

        val response = runCatching {
            rewardsRetrofit.redeemLoyaltyRewards(
                headers,
                mapOf(msisdn.toHeaderPair(), "loyaltyProgramId" to loyaltyProgramId.toLoyaltyId()),
                RedeemLoyaltyRewardsRequestModel(
                    listOf(
                        RewardDetailsRequestModel(
                            rewardsCatalogItem.type,
                            rewardsCatalogItem.id
                        )
                    )
                )
            )
        }.fold(Response<RedeemLoyaltyRewardsResponseModel>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold({
            logSuccessfulNetworkCall()
            LfResult.success(it.result)
        }, {
            logFailedNetworkCall(it)
            LfResult.failure(it.toSpecific())
        })
    }

    override val logTag: String = "redeemLoyaltyRewardsNetworkCall"
}

private fun NetworkError.toSpecific(): RedeemLoyaltyRewardsError {
    if (this is NetworkError.Http && httpStatusCode == 500) {
        if (errorResponse?.error?.code == "50202" && errorResponse?.error?.details?.contains("Capping count threshold exceeded") == true)
            return RedeemLoyaltyRewardsError.CappingCountThresholdExceeded
    }
    return RedeemLoyaltyRewardsError.General(GeneralError.Other(this))
}
