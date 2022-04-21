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
import ph.com.globe.errors.billings.GetBillingsDetailsError
import ph.com.globe.model.balance.*
import ph.com.globe.model.billings.network_models.GetBillingsDetailsParams
import ph.com.globe.model.billings.network_models.GetBillingsDetailsResponse
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetBillingsDetailsNetworkCall @Inject constructor(
    private val billingsRetrofit: BillingsRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: GetBillingsDetailsParams): LfResult<GetBillingsDetailsResponse, GetBillingsDetailsError> {
        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(GetBillingsDetailsError.General(NotLoggedIn))
        }

        val response = kotlin.runCatching {
            billingsRetrofit.getBillingsDetails(
                headers,
                params.accountNumber,
                params.landlineNumber,
                params.mobileNumber,
                params.brandType.toString(),
                params.segment.toString(),
                params.accountType
            )
        }.fold(Response<GetBillingsDetailsResponse>::toLfSdkResult, Throwable::toLFSdkResult)

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

    override val logTag = "GetBillingsDetailsNetworkCall"
}

private fun NetworkError.toSpecific(): GetBillingsDetailsError =
    GetBillingsDetailsError.General(Other(this))
