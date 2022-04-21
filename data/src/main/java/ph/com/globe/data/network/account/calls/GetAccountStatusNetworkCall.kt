/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.account.calls

import okhttp3.Headers
import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.account.AccountRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.account.GetAccountStatusError
import ph.com.globe.model.account.*
import ph.com.globe.model.util.ACCOUNT_STATUS_INACTIVE
import ph.com.globe.model.util.ACCOUNT_STATUS_MIGRATED
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetAccountStatusNetworkCall @Inject constructor(
    private val accountRetrofit: AccountRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: GetAccountStatusParams): LfResult<GetAccountStatusResult, GetAccountStatusError> {

        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(GetAccountStatusError.General(GeneralError.NotLoggedIn))
        }
        val response = kotlin.runCatching {
            accountRetrofit.getAccountStatus(headers, params.toQueryMap())
        }.fold(Response<GetAccountStatusResponse>::toLfSdkResult, Throwable::toLFSdkResult)

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

    override val logTag = "GetAccountStatusNetworkCall"
}

private fun NetworkError.toSpecific(): GetAccountStatusError {
    when (this) {
        is NetworkError.Http -> {
            if (errorResponse?.error?.code == "40005" && errorResponse?.error?.details?.contains("The provided account is not valid") == true) {
                return when {
                    headers.containsAccountStatus(ACCOUNT_STATUS_INACTIVE) -> GetAccountStatusError.InactiveAccount
                    headers.containsAccountStatus(ACCOUNT_STATUS_MIGRATED) -> GetAccountStatusError.NoLongerInSystemAccount
                    else -> GetAccountStatusError.InvalidAccount
                }
            }
            if (errorResponse?.error?.code == "40005") {
                return GetAccountStatusError.InvalidAccount
            }
        }

        else -> Unit
    }
    return GetAccountStatusError.General(GeneralError.Other(this))
}

/**
 * two cases of the value in the headers,
 * 1. statusdescription 2. statusDescription
 */
private val statusDescriptionInHeaders = listOf("statusdescription", "statusDescription")

private fun Headers.containsAccountStatus(status: String): Boolean {
    return get(statusDescriptionInHeaders[0])?.contains(status)
        ?: get(statusDescriptionInHeaders[1])?.contains(status) ?: false
}
