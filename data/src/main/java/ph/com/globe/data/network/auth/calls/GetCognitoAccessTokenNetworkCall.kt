/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.auth.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.auth.CONTENT_TYPE_ENCODED
import ph.com.globe.data.network.auth.G2EncryptionRetrofit
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.globeonesuperapp.data.BuildConfig
import ph.com.globe.model.auth.CognitoAccessTokenResponse
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class GetCognitoAccessTokenNetworkCall @Inject constructor(
    private val cognitoRetrofit: G2EncryptionRetrofit
) : HasLogTag {

    suspend fun execute(): LfResult<CognitoAccessTokenResponse, Unit> {

        val headers = mapOf(
            "Authorization" to "Basic ${BuildConfig.G2_BASIC_AUTH}",
            "Content-Type" to CONTENT_TYPE_ENCODED
        )

        val response = kotlin.runCatching {
            cognitoRetrofit.getCognitoAccessToken(headers = headers)
        }.fold(Response<CognitoAccessTokenResponse>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold({
            LfResult.success(it)
        }, {
            LfResult.failure(Unit)
        })
    }

    override val logTag: String
        get() = "GetCognitoAccessTokenNetworkCall"
}
