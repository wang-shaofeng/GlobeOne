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
import ph.com.globe.errors.group.AddGroupMemberError
import ph.com.globe.model.group.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class AddGroupMemberNetworkCall @Inject constructor(
    private val groupRetrofit: GroupRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    /**
     * Class responsible for Adding a member to the Group Data promo.
     *
     * @param params containing a mobileNumber field needed to execute the API call.
     */
    suspend fun execute(params: AddGroupMemberParams): LfResult<AddGroupMemberResponse, AddGroupMemberError> {
        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(AddGroupMemberError.General(GeneralError.NotLoggedIn))
        }

        val response = kotlin.runCatching {
            groupRetrofit.addGroupMember(headers, params.groupId, params.toNetworkParams())
        }.fold(Response<AddGroupMemberResponse>::toLfSdkResult, Throwable::toLFSdkResult)

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

    override val logTag = "AddGroupMemberNetworkCall"
}

private fun NetworkError.toSpecific(): AddGroupMemberError {

    when (this) {
        is NetworkError.Http -> {
            if (httpStatusCode == 404 && errorResponse?.error?.code == "40402") {
                if (errorResponse?.error?.details == "Pool does not exist") return AddGroupMemberError.PoolNotExist
                if (errorResponse?.error?.details == "Subscriber brand not found") return AddGroupMemberError.SubscriberBrandNotFound
            }

            if (httpStatusCode == 500 && errorResponse?.error?.code == "50202") {
                if (errorResponse?.error?.details == "Subscriber is already member of pool") return AddGroupMemberError.SubscriberAlreadyMember
                if (errorResponse?.error?.details == "Owner cannot be added as pool member") return AddGroupMemberError.OwnerCantBeAdded
                if (errorResponse?.error?.details == "Member limit reached error") return AddGroupMemberError.MemberLimitReached
                if (errorResponse?.error?.details == "Pool is not active") return AddGroupMemberError.PoolNotActive
            }
        }
        else -> Unit
    }

    return AddGroupMemberError.General(GeneralError.Other(this))
}
