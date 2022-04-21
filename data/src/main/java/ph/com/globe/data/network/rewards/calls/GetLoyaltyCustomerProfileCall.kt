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
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.rewards.LoyaltyCustomerProfileError
import ph.com.globe.model.rewards.LoyaltyCustomerProfileModel
import ph.com.globe.model.rewards.LoyaltyCustomerProfileResponseModel
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetLoyaltyCustomerProfileCall @Inject constructor(
    private val rewardsRetrofit: RewardsRetrofit,
    private val tokenRepository: TokenRepository,
) : HasLogTag {

    suspend fun execute(phoneNumber: String): LfResult<LoyaltyCustomerProfileModel, LoyaltyCustomerProfileError> {
        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(LoyaltyCustomerProfileError.General(GeneralError.NotLoggedIn))
        }

        val response = runCatching {
            rewardsRetrofit.getLoyaltyCustomerProfile(headers, phoneNumber)
        }.fold(
            Response<LoyaltyCustomerProfileResponseModel>::toLfSdkResult,
            Throwable::toLFSdkResult
        )

        return response.fold(
            {
                logSuccessfulNetworkCall()
                LfResult.success(LoyaltyCustomerProfileModel(it.result.loyaltyProgramId.toLoyaltyProgramIdEnum()))
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(LoyaltyCustomerProfileError.General(GeneralError.Other(it)))
            }
        )
    }

    override val logTag: String = "GetLoyaltyCustomerProfileCall"
}
