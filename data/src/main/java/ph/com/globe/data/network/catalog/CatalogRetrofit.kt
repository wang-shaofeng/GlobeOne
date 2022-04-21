/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.catalog

import ph.com.globe.model.catalog.*
import retrofit2.Response
import retrofit2.http.*

interface CatalogRetrofit {

    @GET("v2/productOrdering/promos/content/{productId}/status")
    suspend fun getContentSubscriptionStatus(
        @HeaderMap headers: Map<String, String>,
        @Path(value = "productId", encoded = false) productId: String,
        @QueryMap query: Map<String, String>
    ): Response<ContentSubscriptionStatusResponse>

    @POST("v2/productOrdering/promos/content/provision")
    suspend fun provisionContentPromo(
        @HeaderMap headers: Map<String, String>,
        @Body request: ProvisionContentPromoRequest
    ): Response<Unit?>

    @POST("v2/productOrdering/promos/content/deprovision")
    suspend fun unsubscribeContentPromo(
        @HeaderMap headers: Map<String, String>,
        @Body request: UnsubscribeContentPromoRequest
    ): Response<Unit?>
}
