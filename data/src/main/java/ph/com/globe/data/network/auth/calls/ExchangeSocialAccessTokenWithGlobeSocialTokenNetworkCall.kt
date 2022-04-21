/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.auth.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.auth.SignInRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.auth.ExchangeTokenError
import ph.com.globe.model.auth.ExchangeSocialAccessTokenWithGlobeSocialTokenRequestModel
import ph.com.globe.model.auth.ExchangeSocialAccessTokenWithGlobeSocialTokenResponseModel
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class ExchangeSocialAccessTokenWithGlobeSocialTokenNetworkCall @Inject constructor(
    private val authRetrofit: SignInRetrofit,
    private val repository: TokenRepository
) : HasLogTag {

    suspend fun execute(token: String, provider: String): LfResult<String, ExchangeTokenError> {
        val headers = repository.createHeaderWithContentType()
        val response = kotlin.runCatching {
            authRetrofit.exchangeSocialAccessTokenWithGlobeSocialToken(
                headers,
                ExchangeSocialAccessTokenWithGlobeSocialTokenRequestModel(provider, token)
            )
        }.fold(
            Response<ExchangeSocialAccessTokenWithGlobeSocialTokenResponseModel>::toLfSdkResult,
            Throwable::toLFSdkResult
        )

        return response.fold({
            logSuccessfulNetworkCall()
            LfResult.success(it.token)
        }, {
            logFailedNetworkCall(it)
            LfResult.failure(ExchangeTokenError.General(GeneralError.Other(it)))
        })
    }

    override val logTag: String = "ExchangeTokenNetworkCall"
}
