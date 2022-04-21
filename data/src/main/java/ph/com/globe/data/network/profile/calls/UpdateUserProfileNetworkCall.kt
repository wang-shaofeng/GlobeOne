/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.profile.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.profile.ProfileRetrofit
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.profile.UpdateUserProfileError
import ph.com.globe.model.profile.response_models.UpdateUserProfileRequestParams
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class UpdateUserProfileNetworkCall @Inject constructor(
    private val profileRetrofit: ProfileRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: UpdateUserProfileRequestParams): LfResult<Unit, UpdateUserProfileError> {

        val headers = tokenRepository.createAuthenticatedHeader().fold(
            {
                it
            },
            {
                logFailedToFetchAccessToken()
                return LfResult.failure(NetworkError.UserNotLoggedInError.toSpecific())
            }
        )

        val response = kotlin.runCatching {
            profileRetrofit.updateUserProfile(headers, params)
        }.fold(Response<Unit?>::toEmptyLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                LfResult.success(Unit)
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific())
            }
        )
    }

    override val logTag = "UpdateUserProfileNetworkCall"
}

private fun NetworkError.toSpecific(): UpdateUserProfileError = UpdateUserProfileError.General(
    GeneralError.Other(this)
)
