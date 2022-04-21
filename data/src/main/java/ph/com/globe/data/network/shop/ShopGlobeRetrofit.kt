/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.shop

import ph.com.globe.model.shop.GetPromoSubscriptionHistoryResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.QueryMap

interface ShopGlobeRetrofit {

    @GET("v2/productOrdering/subscriptions")
    suspend fun getPromoSubscriptionHistory(
        @HeaderMap headers: Map<String, String>,
        @QueryMap params: Map<String, String>
    ): Response<GetPromoSubscriptionHistoryResponse>
}
