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
import ph.com.globe.errors.billings.GetBillingsStatementsPdfError
import ph.com.globe.model.balance.*
import ph.com.globe.model.billings.network_models.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetBillingsStatementsPdfNetworkCall @Inject constructor(
    private val billingsRetrofit: BillingsRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: GetBillingsStatementsPdfParams): LfResult<GetBillingsStatementsPdfResponse, GetBillingsStatementsPdfError> {
        var headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(GetBillingsStatementsPdfError.General(NotLoggedIn))
        }.plus("Accept" to params.responseType.fromObjectToString())

        if (params.verificationToken != null) {
            headers = headers.plus("VerificationToken" to params.verificationToken.toString())
        }

        val response = if (params.mobileNumber != null)
            kotlin.runCatching {
                billingsRetrofit.getMobileBillingStatementsPdf(
                    headers,
                    params.billingStatementId ?: "",
                    params.accountNumber,
                    params.mobileNumber,
                    params.landlineNumber,
                    params.segment.toString(),
                    params.format
                )
            }.fold(
                Response<GetBillingsStatementsPdfResponse>::toLfSdkResult,
                Throwable::toLFSdkResult
            )
        else
            kotlin.runCatching {
                billingsRetrofit.getBroadbandBillingStatementsPdf(
                    headers,
                    params.landlineNumber,
                    params.segment.toString(),
                    params.format
                )
            }.fold(
                Response<GetBillingsStatementsPdfResponse>::toLfSdkResult,
                Throwable::toLFSdkResult
            )

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

    override val logTag = "GetBillingsStatementsPdfNetworkCall"
}

private fun NetworkError.toSpecific(): GetBillingsStatementsPdfError =
    GetBillingsStatementsPdfError.General(Other(this))
