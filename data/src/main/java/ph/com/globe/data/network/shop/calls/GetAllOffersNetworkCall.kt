/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.shop.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.shop.ShopLFRetrofit
import ph.com.globe.data.network.util.logFailedNetworkCall
import ph.com.globe.data.network.util.logSuccessfulNetworkCall
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.shop.GetAllOffersError
import ph.com.globe.model.shop.*
import ph.com.globe.model.shop.domain_models.*
import ph.com.globe.model.shop.network_models.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import java.util.*
import javax.inject.Inject

class GetAllOffersNetworkCall @Inject constructor(
    private val shopRetrofit: ShopLFRetrofit
) : HasLogTag {

    suspend fun execute(): LfResult<GetAllOffersResponse, GetAllOffersError> {
        val response = kotlin.runCatching {
            shopRetrofit.getAllOffers(GetAllOffersParams())
        }.fold(Response<GetAllOffersResponse>::toLfSdkResult, Throwable::toLFSdkResult)

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

    override val logTag = "GetOffersNetworkCall"
}

private fun NetworkError.toSpecific(): GetAllOffersError =
    GetAllOffersError.General(GeneralError.Other(this))
