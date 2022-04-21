/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.payment.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.payment.PaymentRetrofit
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.errors.GeneralError.Other
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.payment.PurchaseError
import ph.com.globe.model.payment.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class PurchaseLoadNetworkCall @Inject constructor(
    private val paymentRetrofit: PaymentRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: PurchaseParams): LfResult<PurchaseLoadResponse, PurchaseError> {

        val response = when (params.purchaseType) {
            is PurchaseType.BuyLoadConsumer -> {
                kotlin.runCatching {
                    paymentRetrofit.topUpConsumer(
                        tokenRepository.createHeaderWithContentType(),
                        params.targetNumber,
                        PurchaseLoadConsumerRequest(amount = (params.purchaseType as PurchaseType.BuyLoadConsumer).amount)
                    )
                }.fold(Response<PurchaseLoadResponse>::toLfSdkResult, Throwable::toLFSdkResult)
            }
            is PurchaseType.BuyLoadRetailer -> {
                kotlin.runCatching {
                    paymentRetrofit.topUpRetailer(
                        tokenRepository.createHeaderWithContentType(),
                        params.targetNumber,
                        PurchaseLoadRetailerRequest(amount = (params.purchaseType as PurchaseType.BuyLoadRetailer).amount)
                    )
                }.fold(Response<PurchaseLoadResponse>::toLfSdkResult, Throwable::toLFSdkResult)
            }
            else -> {
                return LfResult.failure(PurchaseError.InvalidParameters)
            }
        }
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

    override val logTag = "PurchaseLoadNetworkCall"
}

private fun NetworkError.toSpecific() = PurchaseError.General(Other(this))
