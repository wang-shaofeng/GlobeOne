/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.payment.calls

import okhttp3.ResponseBody
import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.payment.PaymentRetrofit
import ph.com.globe.data.network.util.createAuthenticatedHeader
import ph.com.globe.data.network.util.logFailedToCreateAuthHeader
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.payment.GetPaymentReceiptError
import ph.com.globe.model.payment.GetPaymentReceiptParams
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetPaymentReceiptNetworkCall @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val paymentsRetrofit: PaymentRetrofit
) : HasLogTag {

    suspend fun execute(params: GetPaymentReceiptParams): LfResult<String, GetPaymentReceiptError> {

        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(GetPaymentReceiptError.General(GeneralError.NotLoggedIn))
        }.plus("x-receipt-token" to "Bearer ${params.token}")

        val response = kotlin.runCatching {
            paymentsRetrofit.getPaymentReceipt(
                headers,
                params.receiptId
            )
        }.fold(Response<ResponseBody>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold({
            LfResult.success(it.string())
        }, {
            LfResult.failure(it.toSpecific())
        })
    }

    override val logTag = "GetPaymentReceiptNetworkCall"

    fun NetworkError.toSpecific(): GetPaymentReceiptError {
        return GetPaymentReceiptError.General(GeneralError.Other(this))
    }
}
