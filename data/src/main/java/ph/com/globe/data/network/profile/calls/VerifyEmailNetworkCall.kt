/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.profile.calls

import com.auth0.android.jwt.JWT
import com.google.gson.Gson
import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.profile.ProfileRetrofit
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.data.network.user_details.UserDetailsRepository
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.profile.VerifyEmailError
import ph.com.globe.model.auth.LoginStatus
import ph.com.globe.model.profile.response_models.VerifyEmailCheckJson
import ph.com.globe.model.profile.response_models.VerifyEmailModel
import ph.com.globe.model.profile.response_models.VerifyEmailResponse
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class VerifyEmailNetworkCall @Inject constructor(
    private val profileRetrofit: ProfileRetrofit,
    private val tokenRepository: TokenRepository,
    private val userDetailsRepository: UserDetailsRepository
) : HasLogTag {

    suspend fun execute(verificationToken: String): LfResult<Unit, VerifyEmailError> {
        val header = tokenRepository.createHeaderForAuth()

        val verification = try {
            val verificationDecoded = JWT(verificationToken)
            val verificationCode = verificationDecoded.claims["verificationCode"]?.asString()
            userDetailsRepository.getUserEmail().fold({
                val credentialsJson =
                    Gson().toJson(VerifyEmailCheckJson(email = it, verificationCode!!))
                return@fold encryptCredentials(credentialsJson)
            }, { null })
        } catch (e: Exception) {
            null
        }

        val response = kotlin.runCatching {
            profileRetrofit.verifyEmail(
                header,
                VerifyEmailModel(verification ?: "")
            )
        }.fold(Response<VerifyEmailResponse>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                tokenRepository.setUserToken(it.result.userToken)
                tokenRepository.setLoginStatus(LoginStatus.VERIFIED)
                tokenRepository.setTimeWhenUserTokenWasFetched(System.currentTimeMillis())
                LfResult.success(Unit)
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(VerifyEmailError.General(GeneralError.Other(it)))
            }
        )
    }

    override val logTag: String = "VerifyEmailNetworkCall"
}
