/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.billings.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.billings.BillingsRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError.*
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.billings.GetBillingsStatementsError
import ph.com.globe.model.balance.*
import ph.com.globe.model.billings.network_models.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetBillingsStatementsNetworkCall @Inject constructor(
    private val billingsRetrofit: BillingsRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: GetBillingsStatementsParams): LfResult<GetBillingsStatementsResponse, GetBillingsStatementsError> {
        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(GetBillingsStatementsError.General(NotLoggedIn))
        }

        val response = kotlin.runCatching {
            billingsRetrofit.getBillingsStatements(
                headers,
                params.toQueryMap()
            )
        }.fold(Response<GetBillingsStatementsResponse>::toLfSdkResult, Throwable::toLFSdkResult)

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

    override val logTag = "GetBillingsStatementsNetworkCall"
}

private fun NetworkError.toSpecific(): GetBillingsStatementsError {
    when (this) {
        is NetworkError.Http -> {
            if (errorResponse?.error?.code == "40402" && errorResponse?.error?.details?.contains("No billing statement found") == true)
                return GetBillingsStatementsError.NoBillingStatementFound
        }
        else -> Unit
    }
    return GetBillingsStatementsError.General(Other(this))
}
