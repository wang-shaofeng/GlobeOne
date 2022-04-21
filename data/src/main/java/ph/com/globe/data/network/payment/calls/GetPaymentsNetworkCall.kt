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
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.payment.GetPaymentsError
import ph.com.globe.model.payment.GetPaymentParams
import ph.com.globe.model.payment.GetPaymentsResponse
import ph.com.globe.model.payment.GetPaymentsResult
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetPaymentsNetworkCall @Inject constructor(
    private val paymentRetrofit: PaymentRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: GetPaymentParams): LfResult<GetPaymentsResult, GetPaymentsError> {
        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(GetPaymentsError.General(GeneralError.NotLoggedIn))
        }

        val response = kotlin.runCatching {
            paymentRetrofit.getPayments(
                headers,
                params.mobileNumber,
                params.startDate,
                params.endDate
            )
        }.fold(Response<GetPaymentsResponse>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold({
            logSuccessfulNetworkCall()
            LfResult.success(it.result)
        }, {
            logFailedNetworkCall(it)
            LfResult.failure(it.toSpecific())
        })
    }

    override val logTag = "GetPaymentNetworkCall"

    private fun NetworkError.toSpecific(): GetPaymentsError {
        return GetPaymentsError.General(GeneralError.Other(this))
    }
}
