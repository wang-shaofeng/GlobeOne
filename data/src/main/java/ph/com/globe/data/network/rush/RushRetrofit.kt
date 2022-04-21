/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.rush

import ph.com.globe.globeonesuperapp.data.BuildConfig
import ph.com.globe.model.rush.CreateRushUserRequestModel
import ph.com.globe.model.rush.CreateRushUserResponseModel
import ph.com.globe.model.rush.GetRushAccessTokenResponseModel
import retrofit2.Response
import retrofit2.http.*

interface RushRetrofit {

    @POST("v4/oauth/token")
    suspend fun getRushAccessToken(
        @Query("client_id") clientId: String = BuildConfig.RUSH_CLIENT_ID,
        @Query("client_secret") clientSecret: String = BuildConfig.RUSH_CLIENT_SECRET,
        @Query("scope") scope: String?,
        @Query("grant_type") grantType: String = "client_credentials"
    ): Response<GetRushAccessTokenResponseModel>

    @POST("v4/pos/user_accounts")
    suspend fun createRushUser(
        @Header("Authorization") token: String,
        @Body createRushUserBody: CreateRushUserRequestModel
    ): Response<CreateRushUserResponseModel>

    @GET("v4/pos/user_accounts/{id}")
    suspend fun getRushUserAccountInfo(
        @Path("id") identifier: String,
        @Header("Authorization") token: String
    ): Response<Unit>
}
