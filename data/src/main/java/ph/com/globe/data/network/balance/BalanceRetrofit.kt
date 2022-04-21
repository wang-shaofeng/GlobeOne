/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.balance

import ph.com.globe.model.balance.CheckBalanceSufficiencyResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface BalanceRetrofit {

    @GET("v2/balanceManagement/{msisdn}/balanceSufficiency")
    suspend fun checkPrepaidBalanceSufficiency(
        @Path(value = "msisdn", encoded = false) msisdn: String,
        @Query(value = "amount") amount: String
    ): Response<CheckBalanceSufficiencyResponse>

    @GET("v2/balanceManagement/walletSufficiency")
    suspend fun checkAmaxWalletBalanceSufficiency(
        @QueryMap query: Map<String, String>
    ): Response<CheckBalanceSufficiencyResponse>

}
