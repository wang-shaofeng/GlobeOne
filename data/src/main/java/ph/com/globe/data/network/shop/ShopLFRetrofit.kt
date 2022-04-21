/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.shop

import ph.com.globe.model.shop.network_models.GetAllOffersParams
import ph.com.globe.model.shop.network_models.GetAllOffersResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ShopLFRetrofit {

    @POST("api/v3/catalog/get_offers")
    suspend fun getAllOffers(
        @Body getAllOffersParams: GetAllOffersParams
    ): Response<GetAllOffersResponse>
}
