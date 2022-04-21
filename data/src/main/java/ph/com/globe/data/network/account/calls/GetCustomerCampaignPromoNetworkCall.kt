/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.account.calls

import android.os.Build
import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.account.AccountRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.account.GetCustomerCampaignPromoError
import ph.com.globe.model.account.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.checkValidity
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import java.util.*
import javax.inject.Inject

class GetCustomerCampaignPromoNetworkCall @Inject constructor(
    private val accountRetrofit: AccountRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(
        params: GetCustomerCampaignParams
    ): LfResult<List<AvailableCampaignPromosModel>, GetCustomerCampaignPromoError> {
        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(GetCustomerCampaignPromoError.General(GeneralError.NotLoggedIn))
        }

        val response = runCatching {
            accountRetrofit.getCustomerCampaignPromo(
                headers,
                params.toPersonalizedCampaignQueryMap()
            )
        }.fold(Response<GetCustomerCampaignPromoResponse>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold({
            logSuccessfulNetworkCall()

            val items = it.result.availablePromos.filter { rawItem ->
                rawItem.validityDate.checkValidity(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            }.map { result ->
                result.toModel(params.channel, params.msisdn)
            }

            LfResult.success(items)
        }, {
            logFailedNetworkCall(it)
            when (it.toSpecific()) {
                is GetCustomerCampaignPromoError.General -> LfResult.failure(
                    GetCustomerCampaignPromoError.General(GeneralError.Other(it))
                )
                GetCustomerCampaignPromoError.ResourceNotFound -> LfResult.success(emptyList())
            }
        })
    }

    override val logTag: String = "GetCustomerCampaignPromoNetworkCall"
}

private fun NetworkError.toSpecific(): GetCustomerCampaignPromoError {
    when (this) {
        is NetworkError.Http -> {
            if (httpStatusCode == 404 && errorResponse?.error?.code == "40402" && errorResponse?.error?.details == "Mobile number not found in offers.") {// account doesn't have active personal campaign
                return GetCustomerCampaignPromoError.ResourceNotFound
            }
        }
        else -> Unit
    }
    return GetCustomerCampaignPromoError.General(GeneralError.Other(this))
}
