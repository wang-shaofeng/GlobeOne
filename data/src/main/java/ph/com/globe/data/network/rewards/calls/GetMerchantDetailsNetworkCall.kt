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
import ph.com.globe.errors.rewards.GetMerchantDetailsError
import ph.com.globe.model.rewards.GetMerchantDetailsResponse
import ph.com.globe.model.rewards.GetMerchantDetailsResult
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetMerchantDetailsNetworkCall @Inject constructor(
    private val rewardsRetrofit: RewardsRetrofit,
    private val tokenRepository: TokenRepository,
) : HasLogTag {
    suspend fun executeUsingUUID(
        uuid: String,
    ): LfResult<GetMerchantDetailsResult, GetMerchantDetailsError> {
        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(GetMerchantDetailsError.General(GeneralError.NotLoggedIn))
        }

        val response = runCatching {
            rewardsRetrofit.getMerchantDetails(headers, uuid = uuid)
        }.fold(
            Response<GetMerchantDetailsResponse>::toLfSdkResult,
            Throwable::toLFSdkResult
        )

        return response.fold({
            logSuccessfulNetworkCall()
            LfResult.success(it.result)
        }, {
            logFailedNetworkCall(it)
            LfResult.failure(it.toSpecific())
        })
    }

    suspend fun executeUsingMobileNumber(
        mobileNumber: String,
    ): LfResult<GetMerchantDetailsResult, GetMerchantDetailsError> {
        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(GetMerchantDetailsError.General(GeneralError.NotLoggedIn))
        }

        val response = runCatching {
            rewardsRetrofit.getMerchantDetails(headers, mobileNumber = mobileNumber)
        }.fold(
            Response<GetMerchantDetailsResponse>::toLfSdkResult,
            Throwable::toLFSdkResult
        )

        return response.fold({
            logSuccessfulNetworkCall()
            LfResult.success(it.result)
        }, {
            logFailedNetworkCall(it)
            LfResult.failure(it.toSpecific())
        })
    }

    override val logTag: String = "GetMerchantDetailsNetworkCall"
}

private fun NetworkError.toSpecific(): GetMerchantDetailsError {
    if (this is NetworkError.Http && errorResponse?.error?.code == "40402") {
        return GetMerchantDetailsError.MerchantNotFound
    }
    return GetMerchantDetailsError.General(GeneralError.Other(this))
}
