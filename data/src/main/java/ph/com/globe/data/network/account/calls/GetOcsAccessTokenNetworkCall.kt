/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.account.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.account.AccountOcsRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.createHeaderForOcsAuth
import ph.com.globe.data.network.util.logFailedNetworkCall
import ph.com.globe.data.network.util.logSuccessfulNetworkCall
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.account.GetOcsAccessTokenError
import ph.com.globe.model.account.GetOcsAccessTokenResponse
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class GetOcsAccessTokenNetworkCall @Inject constructor(
    private val accountOcsRetrofit: AccountOcsRetrofit
) : HasLogTag {

    suspend fun execute(): LfResult<String, GetOcsAccessTokenError> {

        val headers = createHeaderForOcsAuth()

        val response = kotlin.runCatching {
            accountOcsRetrofit.getOcsAccessToken(headers)
        }.fold(Response<GetOcsAccessTokenResponse>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                LfResult.success(it.access_token)
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific())
            }
        )
    }

    override val logTag = "GetAccessTokenNetworkCall"
}

private fun NetworkError.toSpecific() = GetOcsAccessTokenError.General(GeneralError.Other(this))
