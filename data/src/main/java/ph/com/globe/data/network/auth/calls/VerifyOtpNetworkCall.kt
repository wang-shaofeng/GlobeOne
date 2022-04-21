/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.auth.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.auth.AuthRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError.Other
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.auth.VerifyOtpError
import ph.com.globe.model.auth.VerifyOtpParams
import ph.com.globe.model.auth.VerifyOtpResult
import ph.com.globe.model.auth.VerifyOtpWithThirdPartyParams
import ph.com.globe.model.auth.toVerifyOtpRequest
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class VerifyOtpNetworkCall @Inject constructor(
    private val authRetrofit: AuthRetrofit,
    private val tokenRepository: TokenRepository
) : HasLogTag {

    suspend fun execute(params: VerifyOtpParams): LfResult<VerifyOtpResult, VerifyOtpError> {

        val headers = if (params.code.length == 4) {
            tokenRepository.createHeaderWithSessionCredentials()
        } else {
            emptyMap()
        }

        var cxsMessageId: String? = null
        val response = kotlin.runCatching {
            if (params.code.length == 4) {
                // If the code length is 4 then we are verifying with the third-party verification parameters
                authRetrofit.verifyOtpWithThirdParty(
                    headers,
                    VerifyOtpWithThirdPartyParams(
                        referenceId = params.referenceId,
                        code = params.code
                    )
                )
            } else {
                authRetrofit.verifyOtp(headers, params.toVerifyOtpRequest())
            }
        }.fold({
            cxsMessageId = it.headers().get("cxsmessageid")
            it.toEmptyLfSdkResult()
        }, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                LfResult.success(VerifyOtpResult(cxsMessageId ?: ""))
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific(cxsMessageId ?: ""))
            }
        )
    }

    override val logTag = "VerifyOtpNetworkCall"
}

private fun NetworkError.toSpecific(cxsMessageId: String): VerifyOtpError {
    when (this) {
        is NetworkError.Http -> {
            when (errorResponse?.error?.code) {
                "50202" -> {
                    when (errorResponse?.error?.details) {
                        "The customer has already reached the max attempt value for verifying OTP." -> return VerifyOtpError.OtpVerifyingMaxAttempt(cxsMessageId)
                        "The OTP is already verified. Cannot verify this OTP again." -> return VerifyOtpError.OtpCodeAlreadyVerified(cxsMessageId)
                        "The OTP is already expired." -> return VerifyOtpError.OtpCodeExpired(cxsMessageId)
                        "The OTP entered is incorrect." -> return VerifyOtpError.OtpCodeIncorrect(cxsMessageId)
                    }
                }
            }
        }
        else -> Unit
    }

    return VerifyOtpError.General(Other(this), cxsMessageId)
}
