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
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.errors.GeneralError.*
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.account.EnrollMigratedAccountsError
import ph.com.globe.model.account.EnrollMigratedAccountsParams
import ph.com.globe.model.account.EnrollMigratedAccountsResponse
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class EnrollMigratedAccountsNetworkCall @Inject constructor(
    private val accountRetrofit: AccountRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: EnrollMigratedAccountsParams): LfResult<EnrollMigratedAccountsResponse, EnrollMigratedAccountsError> {
        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(EnrollMigratedAccountsError.General(NotLoggedIn))
        }

        val response = kotlin.runCatching {
            accountRetrofit.enrollMigratedAccounts(headers, params)
        }.fold(Response<EnrollMigratedAccountsResponse>::toLfSdkResult, Throwable::toLFSdkResult)

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

private fun NetworkError.toSpecific() = EnrollMigratedAccountsError.General(Other(this))
