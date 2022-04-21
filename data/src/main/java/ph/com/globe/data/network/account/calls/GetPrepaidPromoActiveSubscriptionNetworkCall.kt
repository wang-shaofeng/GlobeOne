/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.account.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.account.AccountOcsRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.account.GetPrepaidPromoActiveSubscriptionError
import ph.com.globe.model.account.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class GetPrepaidPromoActiveSubscriptionNetworkCall @Inject constructor(
    private val accountOcsRetrofit: AccountOcsRetrofit
) : HasLogTag {

    suspend fun execute(
        token: String,
        request: GetPrepaidPromoActiveSubscriptionRequest
    ): LfResult<GetPrepaidPromoActiveSubscriptionResponse, GetPrepaidPromoActiveSubscriptionError> {

        val headers =
            mapOf(
                "g-channel" to "SupApp",
                "g-platform" to "app",
                "Content-Type" to "application/json",
                "Authorization" to token
            )

        val response = kotlin.runCatching {
            accountOcsRetrofit.getPrepaidPromoActiveSubscription(headers, request)
        }.fold(
            Response<GetPrepaidPromoActiveSubscriptionResponse>::toLfSdkResult,
            Throwable::toLFSdkResult
        )

        return response.fold({
            logSuccessfulNetworkCall()
            LfResult.success(it)
        }, {
            logFailedNetworkCall(it)
            LfResult.failure(it.toSpecific())
        })
    }

    override val logTag = "RetrieveSubscriberUsageNetworkCall"

}

private fun NetworkError.toSpecific() =
    GetPrepaidPromoActiveSubscriptionError.General(GeneralError.Other(this))
