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
import ph.com.globe.errors.account.GetPostpaidActivePromoSubscriptionError
import ph.com.globe.model.account.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class GetPostpaidActivePromoSubscriptionNetworkCall @Inject constructor(
    private val accountOcsRetrofit: AccountOcsRetrofit
) : HasLogTag {

    suspend fun execute(
        token: String,
        request: GetPostpaidActivePromoSubscriptionRequest
    ): LfResult<GetPostpaidActivePromoSubscriptionResponse, GetPostpaidActivePromoSubscriptionError> {

        val headers =
            mapOf(
                "g-channel" to "SupApp",
                "g-platform" to "app",
                "Content-Type" to "application/json",
                "Authorization" to token
            )

        val response = kotlin.runCatching {
            accountOcsRetrofit.getPostpaidActivePromoSubscription(headers, request)
        }.fold(
            Response<GetPostpaidActivePromoSubscriptionResponse>::toLfSdkResult,
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

    override val logTag = "GetPostpaidActivePromoSubscriptionNetworkCall"
}

private fun NetworkError.toSpecific() =
    GetPostpaidActivePromoSubscriptionError.General(GeneralError.Other(this))
