/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.account.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.account.AccountRetrofit
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toEmptyLfSdkResult
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.account.ModifyEnrolledAccountError
import ph.com.globe.model.account.ModifyEnrolledAccountRequest
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class ModifyEnrolledAccountNetworkCall @Inject constructor(
    private val accountRetrofit: AccountRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(
        accountAlias: String,
        modifiedAccount: ModifyEnrolledAccountRequest
    ): LfResult<Unit, ModifyEnrolledAccountError> {

        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(ModifyEnrolledAccountError.General(GeneralError.NotLoggedIn))
        }

        val response = kotlin.runCatching {
            accountRetrofit.modifyEnrolledAccount(headers, accountAlias, modifiedAccount)
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

    override val logTag = "ModifyEnrolledAccountNetworkCall"
}

private fun NetworkError.toSpecific() = ModifyEnrolledAccountError.General(GeneralError.Other(this))
