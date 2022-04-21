/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.account.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.account.AccountRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.account.GetAccountDetailsError
import ph.com.globe.model.account.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetAccountDetailsNetworkCall @Inject constructor(
    private val accountRetrofit: AccountRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: GetAccountDetailsParams): LfResult<GetAccountDetailsResult, GetAccountDetailsError> {
        val headers = tokenRepository.createHeaderWithReferenceId(params.referenceId, params.verificationType)
            .successOrErrorAction {
                logFailedToCreateAuthHeader()
                return LfResult.failure(GetAccountDetailsError.General(GeneralError.NotLoggedIn))
            }

        val response = kotlin.runCatching {
            accountRetrofit.getAccountDetails(headers, params.toQueryMap())
        }.fold(Response<GetAccountDetailsResponse>::toLfSdkResult, Throwable::toLFSdkResult)

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

    override val logTag = "GetAccountDetailsNetworkCall"
}

private fun NetworkError.toSpecific(): GetAccountDetailsError =
    GetAccountDetailsError.General(GeneralError.Other(this))
