/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.banners

import ph.com.globe.model.banners.BannersResponseModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface BannersRetrofit {

    @GET("/api/mobile/route/dashboard_landing")
    suspend fun getBanners(@QueryMap query: Map<String, String>): Response<BannersResponseModel>
}
