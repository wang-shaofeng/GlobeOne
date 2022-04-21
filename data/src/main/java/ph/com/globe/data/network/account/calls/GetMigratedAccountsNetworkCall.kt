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
import ph.com.globe.errors.GeneralError.*
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.profile.GetEnrolledAccountsError
import ph.com.globe.model.account.GetMigratedAccountsParams
import ph.com.globe.model.account.GetMigratedAccountsResponse
import ph.com.globe.model.account.toListOfEnrolledAccounts
import ph.com.globe.model.account.toQueryMap
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetMigratedAccountsNetworkCall @Inject constructor(
    private val accountRetrofit: AccountRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: GetMigratedAccountsParams): LfResult<List<EnrolledAccount>, GetEnrolledAccountsError> {

        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(GetEnrolledAccountsError.General(NotLoggedIn))
        }
        val queryMap = params.toQueryMap()
        val response = kotlin.runCatching {
            accountRetrofit.getMigratedAccounts(headers, queryMap)
        }.fold(Response<GetMigratedAccountsResponse>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                if (it.result.isEmpty()) LfResult.success(emptyList())
                else LfResult.success(it.result[0].accounts.toListOfEnrolledAccounts())
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific())
            }
        )
    }

    override val logTag = "GetEnrolledAccountsNetworkCall"
}

private fun NetworkError.toSpecific(): GetEnrolledAccountsError {

    when (this) {
        is NetworkError.Http -> {
            if (httpStatusCode == 404 && errorResponse?.error?.code == "40402" && errorResponse?.error?.details == "The user has no enrolled accounts.") {
                return GetEnrolledAccountsError.UserHasNoEnrolledAccounts
            }
        }

        else -> Unit
    }
    return GetEnrolledAccountsError.General(Other(this))
}
