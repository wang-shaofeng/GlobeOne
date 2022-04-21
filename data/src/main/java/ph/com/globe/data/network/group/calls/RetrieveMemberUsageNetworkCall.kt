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
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.group.RetrieveMemberUsageError
import ph.com.globe.model.group.RetrieveMemberUsageParams
import ph.com.globe.model.group.RetrieveMemberUsageResponse
import ph.com.globe.model.group.toQueryMap
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

/**
 * This network call is used to get the member usage information for the provided keyword
 */
class RetrieveMemberUsageNetworkCall @Inject constructor(
    private val groupRetrofit: GroupRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: RetrieveMemberUsageParams): LfResult<RetrieveMemberUsageResponse, RetrieveMemberUsageError> {
        val headers = tokenRepository.createAuthenticatedHeaderForGroup().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(RetrieveMemberUsageError.General(GeneralError.NotLoggedIn))
        }

        val response = kotlin.runCatching {
            groupRetrofit.retrieveMemberUsage(headers, params.toQueryMap())
        }.fold(Response<RetrieveMemberUsageResponse>::toLfSdkResult, Throwable::toLFSdkResult)

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

    override val logTag = "RetrieveMemberUsage"
}

private fun NetworkError.toSpecific() = RetrieveMemberUsageError.General(GeneralError.Other(this))
