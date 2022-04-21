/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.auth.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.auth.AuthRetrofit
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toEmptyLfSdkResult
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.errors.GeneralError.Other
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.auth.AcceptUserAgreementError
import ph.com.globe.model.auth.AcceptUserAgreementParams
import ph.com.globe.model.auth.UserAgreementParams
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class AcceptUserAgreementNetworkCall @Inject constructor(
    private val authRetrofit: AuthRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: AcceptUserAgreementParams): LfResult<Unit, AcceptUserAgreementError> {

        val headers =
            tokenRepository.createHeaderForAuth() + ("Registration-Token" to "Bearer ${params.registerToken}")


        val body = UserAgreementParams(
            params.userAgreementParams.termsAndConditions,
            params.userAgreementParams.privacyPolicy
        )

        val response = kotlin.runCatching {
            authRetrofit.acceptUserAgreement(headers, body)
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

    override val logTag = "AcceptUserAgreementNetworkCall"
}

private fun NetworkError.toSpecific(): AcceptUserAgreementError {
    return AcceptUserAgreementError.General(Other(this))
}
