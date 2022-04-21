/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.auth.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.auth.AuthRetrofit
import ph.com.globe.data.network.auth.model.SendOtpResponse
import ph.com.globe.data.network.auth.model.toSendOtpRequest
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.errors.GeneralError.Other
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.auth.SendOtpError
import ph.com.globe.model.auth.SendOtpParams
import ph.com.globe.model.auth.SendOtpResult
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class SendOtpNetworkCall @Inject constructor(
    private val authRetrofit: AuthRetrofit
) : HasLogTag {

    suspend fun execute(params: SendOtpParams): LfResult<SendOtpResult, SendOtpError> {
        val response = kotlin.runCatching {
            authRetrofit.sendOtp(params.toSendOtpRequest())
        }.fold(Response<SendOtpResponse>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                LfResult.success(it.result)
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific())
            }
        )
    }

    override val logTag = "SendOtpNetworkCall"
}

private fun NetworkError.toSpecific(): SendOtpError {
    when (this) {
        is NetworkError.Http -> {
            if (this.errorResponse?.error?.code == "50202" && this.errorResponse?.error?.details == "The customer is not allowed to request for an OTP at the current moment.")
                return SendOtpError.MaxAttemptsReached
        }
        else -> Unit
    }
    return SendOtpError.General(Other(this))
}
