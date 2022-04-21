/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.account.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.account.AccountRetrofit
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toEmptyLfSdkResult
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.errors.GeneralError.*
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.account.EnrollAccountsError
import ph.com.globe.model.account.EnrollAccountParams
import ph.com.globe.model.account.toEnrollAccountRequest
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class EnrollAccountsNetworkCall @Inject constructor(
    private val accountRetrofit: AccountRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: EnrollAccountParams): LfResult<Unit, EnrollAccountsError> {
        val headers = tokenRepository.createAuthenticatedHeaderWithReferenceId(params.referenceId, params.verificationType).successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(EnrollAccountsError.General(NotLoggedIn))
        }

        val response = kotlin.runCatching {
            accountRetrofit.enrollAccounts(headers, params.toEnrollAccountRequest())
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

    override val logTag = "EnrollAccountsNetworkCall"
}

private fun NetworkError.toSpecific() = EnrollAccountsError.General(Other(this))
