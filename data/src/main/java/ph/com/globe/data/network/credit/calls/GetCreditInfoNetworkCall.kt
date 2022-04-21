/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.credit.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.credit.CreditRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.credit.GetCreditInfoError
import ph.com.globe.model.account.toHeaderPair
import ph.com.globe.model.credit.GetCreditInfoParams
import ph.com.globe.model.credit.GetCreditInfoResponse
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetCreditInfoNetworkCall @Inject constructor(
    private val creditRetrofit: CreditRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: GetCreditInfoParams): LfResult<GetCreditInfoResponse, GetCreditInfoError> {
        val headers = tokenRepository.createHeaderWithReferenceId(params.otpReferenceId)
            .successOrErrorAction {
                logFailedToCreateAuthHeader()
                return LfResult.failure(GetCreditInfoError.General(GeneralError.NotLoggedIn))
            }

        val response = kotlin.runCatching {
            creditRetrofit.getCreditInfo(
                headers,
                mapOf(params.mobileNumber.toHeaderPair())
            )
        }.fold(Response<GetCreditInfoResponse>::toLfSdkResult, Throwable::toLFSdkResult)

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

    override val logTag = "GetCreditInfoSessionNetworkCall"
}

private fun NetworkError.toSpecific(): GetCreditInfoError {

    when (this) {
        is NetworkError.Http -> {
            if (httpStatusCode == 404 && errorResponse?.error?.code == "40402" && errorResponse?.error?.details == "The customer has no loan.") {
                return GetCreditInfoError.NoLoan
            }
            if (httpStatusCode == 404 && errorResponse?.error?.code == "40402" && errorResponse?.error?.details == "The requested resource is not found.") {
                return GetCreditInfoError.ResourceNotFound
            }
        }

        else -> Unit
    }
    return GetCreditInfoError.General(GeneralError.Other(this))
}
