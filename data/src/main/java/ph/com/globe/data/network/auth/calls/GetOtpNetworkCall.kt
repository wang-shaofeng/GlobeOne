/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.auth.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.auth.AuthRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.auth.GetOtpError
import ph.com.globe.model.auth.GetOtpParams
import ph.com.globe.model.auth.GetOtpResponse
import ph.com.globe.model.auth.toQueryMap
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class GetOtpNetworkCall @Inject constructor(
    private val authRetrofit: AuthRetrofit
) : HasLogTag {

    suspend fun execute(params: GetOtpParams): LfResult<GetOtpResponse, GetOtpError> {
        val response = kotlin.runCatching {
            authRetrofit.getOtp(params.toQueryMap(), params.categoryIdentifiers)
        }.fold(Response<GetOtpResponse>::toLfSdkResult, Throwable::toLFSdkResult)

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

    override val logTag = "GetOtpNetworkCall"
}

private fun NetworkError.toSpecific() = GetOtpError.General(GeneralError.Other(this))
