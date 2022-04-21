/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.account

import ph.com.globe.model.account.*
import ph.com.globe.model.account.network_models.GetPrepaidPromoSubscriptionUsageRequest
import ph.com.globe.model.account.network_models.GetPrepaidPromoSubscriptionUsageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface AccountOcsRetrofit {

    @POST("security/v1/access-token-generation")
    suspend fun getOcsAccessToken(
        @HeaderMap headers: Map<String, String>
    ): Response<GetOcsAccessTokenResponse>

    @POST("prepaid/v1/account/promo-subscription-usage")
    suspend fun getPrepaidPromoSubscriptionUsage(
        @HeaderMap headers: Map<String, String>,
        @Body request: GetPrepaidPromoSubscriptionUsageRequest
    ): Response<GetPrepaidPromoSubscriptionUsageResponse>

    @POST("prepaid/v1/account/active-promo-subscriptions")
    suspend fun getPrepaidPromoActiveSubscription(
        @HeaderMap headers: Map<String, String>,
        @Body request: GetPrepaidPromoActiveSubscriptionRequest
    ): Response<GetPrepaidPromoActiveSubscriptionResponse>

    @POST("postpaid/v1/account/promo-subscription-usage")
    suspend fun getPostpaidPromoSubscriptionUsage(
        @HeaderMap headers: Map<String, String>,
        @Body request: GetPostpaidPromoSubscriptionUsageRequest
    ): Response<GetPostpaidPromoSubscriptionUsageResponse>

    @POST("postpaid/v1/account/active-promo-subscriptions")
    suspend fun getPostpaidActivePromoSubscription(
        @HeaderMap headers: Map<String, String>,
        @Body request: GetPostpaidActivePromoSubscriptionRequest
    ): Response<GetPostpaidActivePromoSubscriptionResponse>
}
