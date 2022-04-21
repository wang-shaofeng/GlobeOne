/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.profile.calls

import com.google.gson.Gson
import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.profile.ProfileRetrofit
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.data.network.user_details.UserDetailsRepository
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.profile.SendVerificationEmailError
import ph.com.globe.model.profile.*
import ph.com.globe.model.profile.response_models.SendVerificationEmailBody
import ph.com.globe.model.profile.response_models.SendVerificationEmailEncryptedBody
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class SendVerificationEmailNetworkCall @Inject constructor(
    private val profileRetrofit: ProfileRetrofit,
    private val tokenRepository: TokenRepository,
    private val userDetailsRepository: UserDetailsRepository
) : HasLogTag {

    suspend fun execute(): LfResult<Unit, SendVerificationEmailError> {

        val headers = tokenRepository.createHeaderForAuth()

        userDetailsRepository.getUserEmail().fold({ email ->
            val verificationJson = Gson().toJson(SendVerificationEmailEncryptedBody(email))
            val verification = encryptCredentials(verificationJson)
            val body = SendVerificationEmailBody(verification)
            val response = kotlin.runCatching {
                profileRetrofit.resendEmailVerification(
                    headers,
                    body
                )
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
        }, {
            return LfResult.failure(NetworkError.UserNotLoggedInError.toSpecific())
        })
    }

    override val logTag = "VerifyEmailNetworkCall"
}

private fun NetworkError.toSpecific(): SendVerificationEmailError =
    if (this is NetworkError.Http && this.errorResponse?.error?.code == "50202"
        && errorResponse?.error?.details == "Email is already verified.")
        SendVerificationEmailError.EmailIsAlreadyVerified
    else SendVerificationEmailError.General(GeneralError.Other(this))
