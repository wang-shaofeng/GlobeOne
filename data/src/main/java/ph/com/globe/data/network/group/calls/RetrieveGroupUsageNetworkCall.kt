/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.group.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.group.GroupRetrofit
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.group.RetrieveGroupUsageError
import ph.com.globe.model.group.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class RetrieveGroupUsageNetworkCall @Inject constructor(
    private val groupRetrofit: GroupRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: RetrieveGroupUsageParams): LfResult<RetrieveGroupUsageResponse, RetrieveGroupUsageError> {
        val headers = tokenRepository.createAuthenticatedHeaderForGroup().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(RetrieveGroupUsageError.General(GeneralError.NotLoggedIn))
        }.plus("Source" to "CXS")

        val response = kotlin.runCatching {
            groupRetrofit.retrieveGroupUsage(
                headers,
                params.toQueryMap()
            )
        }.fold(Response<RetrieveGroupUsageResponse>::toLfSdkResult, Throwable::toLFSdkResult)

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

    override val logTag = "RetrieveGroupUsageNetworkCall"
}

private fun NetworkError.toSpecific(): RetrieveGroupUsageError {
    when (this) {
        is NetworkError.Http -> {
            if (httpStatusCode == 500 && errorResponse?.error?.code == "50202" && errorResponse?.error?.details == "The subscriber does not belong to any pool") return RetrieveGroupUsageError.SubscriberNotBelongToAnyPool

            if (httpStatusCode == 404 && errorResponse?.error?.code == "40202") {
                if (errorResponse?.error?.details == "The mobile number is not found.") return RetrieveGroupUsageError.MobileNumberNotFound
                if (errorResponse?.error?.details == "Group doesn’t exist.") return RetrieveGroupUsageError.GroupNotExist
                if (errorResponse?.error?.details == "Wallet not found.") return RetrieveGroupUsageError.WalletNotFound
            }
        }

        else -> Unit
    }

    return RetrieveGroupUsageError.General(GeneralError.Other(this))
}
