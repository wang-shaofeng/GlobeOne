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
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.group.SetMemberUsageLimitError
import ph.com.globe.model.group.SetMemberUsageLimitParams
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class SetMemberUsageLimitNetworkCall @Inject constructor(
    private val groupRetrofit: GroupRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: SetMemberUsageLimitParams): LfResult<Unit, SetMemberUsageLimitError> {
        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(SetMemberUsageLimitError.General(GeneralError.NotLoggedIn))
        }.plus("Source" to "CXS")

        val response = kotlin.runCatching {
            groupRetrofit.setMemberUsageLimit(headers, params)
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

    override val logTag = "GetGroupListNetworkCall"
}

private fun NetworkError.toSpecific(): SetMemberUsageLimitError {
    when (this) {
        is NetworkError.Http -> {
            if (httpStatusCode == 404 && errorResponse?.error?.code == "40402") {
                if (errorResponse?.error?.details == "Group member doesn't exist.") return SetMemberUsageLimitError.GroupMemberNotExist
                if (errorResponse?.error?.details == "Group doesn't exist.") return SetMemberUsageLimitError.GroupNotExist
                if (errorResponse?.error?.details == "Wallet not found.") return SetMemberUsageLimitError.WalletNotFound
                if (errorResponse?.error?.details == "Subscriber not found.") return SetMemberUsageLimitError.SubscriberNotFound
            }

            if (httpStatusCode == 500 && errorResponse?.error?.code == "50202" && errorResponse?.error?.details == "Exceeded total usage limit.") return SetMemberUsageLimitError.ExceededTotalUsageLimit
        }

        else -> Unit
    }

    return SetMemberUsageLimitError.General(GeneralError.Other(this))
}
