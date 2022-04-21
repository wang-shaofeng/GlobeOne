/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.auth

import ph.com.globe.model.auth.ExchangeSocialAccessTokenWithGlobeSocialTokenRequestModel
import ph.com.globe.model.auth.ExchangeSocialAccessTokenWithGlobeSocialTokenResponseModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface SignInRetrofit {
    @POST("signin/oauth_token")
    suspend fun exchangeSocialAccessTokenWithGlobeSocialToken(
        @HeaderMap headers: Map<String, String>,
        @Body ExchangeSocialAccessTokenWithGlobeSocialTokenRequestModel: ExchangeSocialAccessTokenWithGlobeSocialTokenRequestModel
    ): Response<ExchangeSocialAccessTokenWithGlobeSocialTokenResponseModel>

}
