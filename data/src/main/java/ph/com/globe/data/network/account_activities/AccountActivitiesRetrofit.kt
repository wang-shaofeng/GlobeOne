/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.account_activities

import ph.com.globe.model.account_activities.AccountRewardsResponseModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Path
import retrofit2.http.Query

interface AccountActivitiesRetrofit {
    @GET("v2/loyaltyManagement/subscribers/{subscriberId}/transactions")
    suspend fun getLoyaltySubscribersTransactionHistory(
        @HeaderMap headers: Map<String, String>,
        @Path(value = "subscriberId", encoded = false) msisdn: String,
        @Query("dateFrom") dateFrom: String,
        @Query("dateTo") dateTo: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int = 10,
        @Query("subscriberType") subscriberType: Int = 1
    ): Response<AccountRewardsResponseModel>
}
