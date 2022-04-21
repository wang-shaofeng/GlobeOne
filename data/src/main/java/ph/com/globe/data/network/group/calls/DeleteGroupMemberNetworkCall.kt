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
import ph.com.globe.errors.group.DeleteGroupMemberError
import ph.com.globe.model.group.DeleteGroupMemberParams
import ph.com.globe.model.group.DeleteGroupMemberResponse
import ph.com.globe.model.group.toNetworkParams
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

/**
 * Class responsible for
 */
class DeleteGroupMemberNetworkCall @Inject constructor(
    private val groupRetrofit: GroupRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: DeleteGroupMemberParams): LfResult<DeleteGroupMemberResponse, DeleteGroupMemberError> {
        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(DeleteGroupMemberError.General(GeneralError.NotLoggedIn))
        }

        val response = kotlin.runCatching {
            groupRetrofit.deleteGroupMember(
                headers = headers,
                groupId = params.groupId,
                removeMemberParams = params.toNetworkParams()
            )
        }.fold(Response<DeleteGroupMemberResponse>::toLfSdkResult, Throwable::toLFSdkResult)

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

private fun NetworkError.toSpecific(): DeleteGroupMemberError {
    when (this) {
        is NetworkError.Http -> {
            if (httpStatusCode == 404 && errorResponse?.error?.code == "40402" && errorResponse?.error?.details == "Group doesn’t exist.") return DeleteGroupMemberError.GroupNotExist
            if (httpStatusCode == 500 && errorResponse?.error?.code == "50202" && errorResponse?.error?.details == "Group member doesn’t exist.") return DeleteGroupMemberError.GroupMemberNotExist
        }

        else -> Unit
    }

    return DeleteGroupMemberError.General(GeneralError.Other(this))
}
