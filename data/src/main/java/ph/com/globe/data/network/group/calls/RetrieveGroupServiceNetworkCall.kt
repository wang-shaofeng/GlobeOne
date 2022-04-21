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
import ph.com.globe.errors.group.RetrieveGroupServiceError
import ph.com.globe.model.group.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

/**
 * Class responsible for getting the list of the groups the user is a member/owner of.
 * Within the response we can find [RetrieveGroupServiceResult.wallets] array that will contain information about the group data participation.
 * From the response we use [RetrieveGroupServiceWalletItem.id] as a keyword to execute [RetrieveMemberUsageNetworkCall] for members or [RetrieveGroupUsageNetworkCall] for owners use case.
 */
class RetrieveGroupServiceNetworkCall @Inject constructor(
    private val groupRetrofit: GroupRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: RetrieveGroupServiceParams): LfResult<RetrieveGroupServiceResponse, RetrieveGroupServiceError> {
        val headers = tokenRepository.createAuthenticatedHeaderForGroup().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(RetrieveGroupServiceError.General(GeneralError.NotLoggedIn))
        }.plus("Source" to "CXS")

        val response = kotlin.runCatching {
            groupRetrofit.retrieveGroupService(
                headers,
                params.toQueryMap()
            )
        }.fold(Response<RetrieveGroupServiceResponse>::toLfSdkResult, Throwable::toLFSdkResult)

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

private fun NetworkError.toSpecific(): RetrieveGroupServiceError {
    when (this) {
        is NetworkError.Http -> {
            if (httpStatusCode == 404 && errorResponse?.error?.code == "40402" && errorResponse?.error?.details == "The mobile number is not found.") return RetrieveGroupServiceError.MobileNumberNotFound
        }
        else -> Unit
    }

    return RetrieveGroupServiceError.General(GeneralError.Other(this))
}
