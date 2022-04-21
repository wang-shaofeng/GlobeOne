/*
  * Copyright (C) 2021 LotusFlare
  * All Rights Reserved.
  * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
  */

package ph.com.globe.data.network.shop

import ph.com.globe.model.shop.ValidateRetailerRequest
import ph.com.globe.model.shop.ValidateRetailerResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface OCSShopRetrofit {
    @POST("user/v1/account/retailer/validation")
    suspend fun validateRetailer(
        @HeaderMap header: Map<String, String>,
        @Body body: ValidateRetailerRequest
    ): Response<ValidateRetailerResponse>
}
