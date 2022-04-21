/*
  * Copyright (C) 2021 LotusFlare
  * All Rights Reserved.
  * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
  */

package ph.com.globe.data.network.shop.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.shop.OCSShopRetrofit
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.data.network.util.createHeaderWithContentType
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.shop.ValidateRetailerError
import ph.com.globe.model.shop.ValidateRetailerRequest
import ph.com.globe.model.shop.ValidateRetailerResponse
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class ValidateRetailerNetworkCall @Inject constructor(
    private val ocsShopRetrofit: OCSShopRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {
    override val logTag = "ValidateRetailerNetworkCall"

    suspend fun execute(serviceNumber: String): LfResult<Boolean, ValidateRetailerError> {
        val header = tokenRepository.createHeaderWithContentType()
        val response = kotlin.runCatching {
            ocsShopRetrofit.validateRetailer(header, ValidateRetailerRequest(serviceNumber))
        }.fold(Response<ValidateRetailerResponse>::toLfSdkResult, Throwable::toLFSdkResult)
        return response.fold({
            LfResult.success(it.isRetailer)
        }, {
            LfResult.failure(it.toSpecific())
        })
    }
}

private fun NetworkError.toSpecific(): ValidateRetailerError =
    ValidateRetailerError.General(GeneralError.Other(this))
