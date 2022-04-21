/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.shop.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.shop.ShopGlobeRetrofit
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.GeneralError.Other
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.shop.GetPromoSubscriptionHistoryError
import ph.com.globe.model.shop.GetPromoSubscriptionHistoryParams
import ph.com.globe.model.shop.GetPromoSubscriptionHistoryResponse
import ph.com.globe.model.shop.toQueryMap
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetPromoSubscriptionHistoryNetworkCall @Inject constructor(
    private val shopRetrofit: ShopGlobeRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: GetPromoSubscriptionHistoryParams): LfResult<GetPromoSubscriptionHistoryResponse, GetPromoSubscriptionHistoryError> {
        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(GetPromoSubscriptionHistoryError.General(GeneralError.NotLoggedIn))
        }

        val response = kotlin.runCatching {
            shopRetrofit.getPromoSubscriptionHistory(
                headers = headers,
                params = params.toQueryMap()
            )
        }.fold(
            Response<GetPromoSubscriptionHistoryResponse>::toLfSdkResult,
            Throwable::toLFSdkResult
        )

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

    override val logTag = "GetPromoSubscriptionHistoryNetworkCall"
}

private fun NetworkError.toSpecific(): GetPromoSubscriptionHistoryError {

    when (this) {
        is NetworkError.Http -> {
            if (errorResponse?.error?.code == "40402" && errorResponse?.error?.details == "No subscriptions found.") {
                return GetPromoSubscriptionHistoryError.NoSubscriptionsFound
            }
        }

        else -> Unit
    }
    return GetPromoSubscriptionHistoryError.General(Other(this))
}
