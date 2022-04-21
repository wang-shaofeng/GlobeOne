/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.payment.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.payment.PaymentRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.data.shared_preferences.token.utils.getUUID
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.GeneralError.Other
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.payment.PaymentError
import ph.com.globe.model.payment.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import java.util.*
import javax.inject.Inject

class GetPaymentMethodNetworkCall @Inject constructor(
    private val paymentRetrofit: PaymentRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(): LfResult<List<CreditCardModel>, PaymentError> {

        val headers =
            tokenRepository.createAuthenticatedHeader().fold({
                it
            }, {
                return LfResult.failure(PaymentError.General(GeneralError.NotLoggedIn))
            })
        val response = kotlin.runCatching {
            paymentRetrofit.getPaymentMethod(
                headers,
                tokenRepository.getUUID() ?: ""
            )
        }.fold(Response<GetPaymentMethodResponse>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                LfResult.success(it.result.creditCards)
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific())
            }
        )
    }

    override val logTag = "GetPaymentMethodNetworkCall"
}

private fun NetworkError.toSpecific() = PaymentError.General(Other(this))
