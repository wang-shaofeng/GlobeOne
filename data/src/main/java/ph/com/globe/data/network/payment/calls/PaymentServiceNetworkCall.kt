/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.payment.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.payment.PaymentRetrofit
import ph.com.globe.data.network.payment.model.PaymentServiceResponse
import ph.com.globe.data.network.payment.model.toPaymentServiceRequestBody
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError.Other
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.payment.PaymentServiceError
import ph.com.globe.model.payment.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class PaymentServiceNetworkCall @Inject constructor(
    private val paymentRetrofit: PaymentRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: PaymentServiceParams): LfResult<PaymentServiceResult, PaymentServiceError> {

        val response = kotlin.runCatching {
            paymentRetrofit.paymentService(
                tokenRepository.createHeaderWithContentType(),
                // TODO params.toPaymentServiceRequest(tokenRepository.getUUID())
                // send UUID when server will support it
                params.toPaymentServiceRequestBody(null)
            )
        }.fold(Response<PaymentServiceResponse>::toLfSdkResult, Throwable::toLFSdkResult)

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

    override val logTag = "PaymentServiceNetworkCall"
}

private fun NetworkError.toSpecific() =
    when (this) {
        is NetworkError.NoInternet -> PaymentServiceError.NoInternetConnectionError
        else -> PaymentServiceError.General(Other(this))
    }
