/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.auth.calls

import okhttp3.MediaType
import okhttp3.RequestBody
import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.auth.AuthRetrofit
import ph.com.globe.data.network.util.logFailedNetworkCall
import ph.com.globe.data.network.util.logSuccessfulNetworkCall
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.errors.GeneralError.Other
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.auth.GetAccessTokenError
import ph.com.globe.globeonesuperapp.data.BuildConfig
import ph.com.globe.model.auth.GetAccessTokenResponse
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class GetAccessTokenNetworkCall @Inject constructor(
    private val authRetrofit: AuthRetrofit
) : HasLogTag {

    fun execute(): LfResult<String, GetAccessTokenError> {

        val headers =
            mapOf("Authorization" to "Basic ${BuildConfig.OAUTH_CREDENTIALS}")

        val body = RequestBody.create(MediaType.parse("application/json"), "{}")

        val response = kotlin.runCatching {
            authRetrofit.getAccessToken(headers, body).execute()
        }.fold(Response<GetAccessTokenResponse>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                LfResult.success(it.result.accessToken)
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific())
            }
        )
    }

    override val logTag = "GetAccessTokenNetworkCall"
}

private fun NetworkError.toSpecific() = GetAccessTokenError.General(Other(this))
