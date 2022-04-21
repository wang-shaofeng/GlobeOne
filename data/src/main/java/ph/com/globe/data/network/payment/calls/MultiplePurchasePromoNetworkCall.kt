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
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.GeneralError.Other
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.payment.PurchaseError
import ph.com.globe.model.payment.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class MultiplePurchasePromoNetworkCall @Inject constructor(
    private val paymentRetrofit: PaymentRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: PurchaseParams): LfResult<MultiplePurchasePromoResult, PurchaseError> {

        val request = MultiplePurchasePromoRequest(
            params.targetNumber,
            params.transformToTransactions()
        )
        val header = tokenRepository.createHeaderWithReferenceId(params.otpReferenceId)
            .successOrNull() // if we don't have the referenceId and we are not logged in, we will return NotLoggedIn error
            ?: return LfResult.failure(PurchaseError.General(GeneralError.NotLoggedIn))

        val response = kotlin.runCatching {
            paymentRetrofit.multiplePurchasePromo(
                header,
                request
            )
        }.fold(Response<MultiplePurchasePromoResponse>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                LfResult.success(it.result)
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific())
            }
        )
    }

    override val logTag = "MultiplePurchasePromoNetworkCall"
}

private fun NetworkError.toSpecific() = PurchaseError.General(Other(this))
