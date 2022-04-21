/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.profile.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.profile.ProfileRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError.*
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.profile.GetEnrolledAccountsError
import ph.com.globe.model.profile.response_models.EnrolledAccountJson
import ph.com.globe.model.profile.response_models.GetEnrolledAccountsResponse
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetEnrolledAccountsNetworkCall @Inject constructor(
    private val profileRetrofit: ProfileRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(): LfResult<List<EnrolledAccountJson>, GetEnrolledAccountsError> {

        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(GetEnrolledAccountsError.General(NotLoggedIn))
        }

        val response = kotlin.runCatching {
            profileRetrofit.getEnrolledAccounts(headers)
        }.fold(Response<GetEnrolledAccountsResponse>::toLfSdkResult, Throwable::toLFSdkResult)

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

    override val logTag = "GetEnrolledAccountsNetworkCall"
}

private fun NetworkError.toSpecific(): GetEnrolledAccountsError {
    when (this) {
        is NetworkError.Http -> {
            if (httpStatusCode == 404 &&
                errorResponse?.error?.code == "40402" &&
                errorResponse?.error?.details?.contains("The user has no enrolled accounts") == true
            ) {
                return GetEnrolledAccountsError.UserHasNoEnrolledAccounts
            }
        }

        else -> Unit
    }
    return GetEnrolledAccountsError.General(Other(this))
}
