/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.rush.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.rush.RushRetrofit
import ph.com.globe.data.network.rush.RushTokenManager
import ph.com.globe.data.network.util.logFailedNetworkCall
import ph.com.globe.data.network.util.logSuccessfulNetworkCall
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.rush.GetRushAccessTokenError
import ph.com.globe.model.rush.GetRushAccessTokenResponseModel
import ph.com.globe.model.rush.GetRushUserAccessTokenParams
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class GetRushUserAccessTokenNetworkCall @Inject constructor(
    private val rushRetrofit: RushRetrofit,
    private val rushTokenManager: RushTokenManager
) : HasLogTag {

    suspend fun execute(params: GetRushUserAccessTokenParams): LfResult<GetRushAccessTokenResponseModel, GetRushAccessTokenError> {
        rushTokenManager.getStoredRushAccessToken()?.let {
            return LfResult.success(it)
        }
        val identifiedQueryParam = "user_account(identifier:${params.identifier})"
        val response = kotlin.runCatching {
            rushRetrofit.getRushAccessToken(scope = identifiedQueryParam)
        }.fold(Response<GetRushAccessTokenResponseModel>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            {
                val rushTokenToStore = GetRushAccessTokenResponseModel(
                    it.access_token,
                    it.expires_in,
                    System.currentTimeMillis()
                )
                rushTokenManager.storeRushAccessToken(rushTokenToStore)
                logSuccessfulNetworkCall()
                LfResult.success(it)
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific())
            }
        )
    }

    override val logTag = "GetRushUserAccessTokenNetworkCall"
}

private fun NetworkError.toSpecific() = GetRushAccessTokenError.General(GeneralError.Other(this))
