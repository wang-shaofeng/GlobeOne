/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.auth

import ph.com.globe.globeonesuperapp.data.BuildConfig
import ph.com.globe.model.auth.CognitoAccessTokenResponse
import ph.com.globe.model.auth.SymmetricKeyResponse
import retrofit2.Response
import retrofit2.http.*

interface G2EncryptionRetrofit {

    @POST("oauth2/token")
    suspend fun getCognitoAccessToken(
        @HeaderMap headers: Map<String, String>,
        @Query("grant_type") grantType: String = GRANT_TYPE
    ): Response<CognitoAccessTokenResponse>

    @GET("client/keymgmt/keys/symmetric")
    suspend fun getSymmetricKey(
        @Header("Authorization") auth: String,
        @Header("Content-Type") contentType: String = CONTENT_TYPE_JSON,
        @Header("x-api-key") xApiKey: String = BuildConfig.G2_API_KEY,
        @Header("app-id") appId: String = BuildConfig.G2_APP_ID,
        @Header("user-id") userId: String = BuildConfig.G2_USER_ID,
        @Header("algorithm") algorithm: String = ALGORITHM,
    ): Response<SymmetricKeyResponse>

}

const val GRANT_TYPE = "client_credentials"
const val ALGORITHM = "AES"
const val CONTENT_TYPE_JSON = "application/json"
const val CONTENT_TYPE_ENCODED = "application/x-www-form-urlencoded"
