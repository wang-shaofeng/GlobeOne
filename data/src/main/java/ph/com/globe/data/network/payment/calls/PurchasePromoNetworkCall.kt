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
import ph.com.globe.errors.GeneralError.Other
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.payment.PurchaseError
import ph.com.globe.model.payment.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class PurchasePromoNetworkCall @Inject constructor(
    private val paymentRetrofit: PaymentRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: PurchaseParams): LfResult<Unit, PurchaseError> {

        val request = if (params.chargeToLoad) {
            PurchasePromoRequest(
                params.targetNumber,
                (params.purchaseType as PurchaseType.BuyPromo).chargePromoServiceId
            )
        } else {
            PurchasePromoRequest(
                params.targetNumber,
                (params.purchaseType as PurchaseType.BuyPromo).nonChargePromoServiceId
            )
        }

        val response = kotlin.runCatching {
            paymentRetrofit.purchasePromoByServiceId(
                tokenRepository.createHeaderWithContentType(),
                request
            )
        }.fold(Response<Unit?>::toEmptyLfSdkResult, Throwable::toLFSdkResult)

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

    override val logTag = "PurchasePromoNetworkCall"
}

private fun NetworkError.toSpecific() = PurchaseError.General(Other(this))
