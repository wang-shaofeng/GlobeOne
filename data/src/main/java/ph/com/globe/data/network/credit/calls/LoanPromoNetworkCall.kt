/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.credit.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.credit.CreditRetrofit
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.credit.LoanPromoError
import ph.com.globe.model.credit.LoanPromoParams
import ph.com.globe.model.credit.toHeadersMap
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class LoanPromoNetworkCall @Inject constructor(
    private val creditRetrofit: CreditRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: LoanPromoParams): LfResult<Unit, LoanPromoError> {
        val response = kotlin.runCatching {
            if (params.referenceId != null)
                creditRetrofit.loanPromo(params.toHeadersMap(), params.loanPromoRequest)
            else {
                val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
                    logFailedToCreateAuthHeader()
                    return LfResult.failure(LoanPromoError.General(GeneralError.NotLoggedIn))
                }
                creditRetrofit.loanPromo(headers, params.loanPromoRequest)
            }
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

    override val logTag = "LoanPromoNetworkCall"
}

private fun NetworkError.toSpecific(): LoanPromoError =
    LoanPromoError.General(GeneralError.Other(this))
