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
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.payment.GetGcashBalanceError
import ph.com.globe.model.payment.GetGCashBalanceResponse
import ph.com.globe.model.payment.GetGCashBalanceResult
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetGCashBalanceNetworkCall @Inject constructor(
    private val paymentRetrofit: PaymentRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(mobileNumber: String): LfResult<GetGCashBalanceResult, GetGcashBalanceError> {

        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(GetGcashBalanceError.General(GeneralError.NotLoggedIn))
        }

        val response = kotlin.runCatching {
            paymentRetrofit.getGCashBalance(headers, mobileNumber)
        }.fold(Response<GetGCashBalanceResponse>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                LfResult.success(it.result[0])
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific())
            }
        )
    }

    override val logTag = "GetGCashBalanceNetworkCall"
}

private fun NetworkError.toSpecific(): GetGcashBalanceError {
    when (this) {
        is NetworkError.Http -> {
            if (errorResponse?.error?.code == "40303" && errorResponse?.error?.details == "The mobile number is not linked with gcash.")
                return GetGcashBalanceError.GCashNotLinked
            if (errorResponse?.error?.code == "40402" && errorResponse?.error?.details == "User does not exist")
                return GetGcashBalanceError.NoGCashAccount
            if (errorResponse?.error?.code == "50202" && errorResponse?.error?.details == "User is not registered/active")
                return GetGcashBalanceError.SuspendedOrInactive
        }

        else -> Unit
    }
    return GetGcashBalanceError.General(GeneralError.Other(this))
}
