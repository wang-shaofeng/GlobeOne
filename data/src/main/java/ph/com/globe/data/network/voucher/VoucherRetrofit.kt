/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.voucher

import ph.com.globe.model.voucher.LoyaltySubscriberCouponDetailsResponse
import ph.com.globe.model.voucher.MarkVoucherAsUsedRequest
import ph.com.globe.model.voucher.PromoVouchersResponse
import ph.com.globe.model.voucher.RetrieveUsedVouchersResponse
import retrofit2.Response
import retrofit2.http.*

interface VoucherRetrofit {

    @GET("v2/loyaltyManagement/subscribers/{subscriberId}/coupons")
    suspend fun getLoyaltySubscribersCouponDetails(
        @HeaderMap headers: Map<String, String>,
        @Path(value = SUBSCRIBER_ID, encoded = false) subscriberId: String,
        @Query(value = SUBSCRIBER_TYPE, encoded = false) subscriberType: Int,
        @Query(value = CHANNEL, encoded = false) channel: String,
        @Query(value = EXPIRY_DATE_FROM, encoded = false) expiryDateFrom: String?,
        @Query(value = EXPIRY_DATE_TO, encoded = false) expiryDateTo: String?,
        @Query(value = OFFSET, encoded = false) offset: Int?,
        @Query(value = LIMIT, encoded = false) limit: Int?
    ): Response<LoyaltySubscriberCouponDetailsResponse>

    @POST("v1/paymentMethods/promoVouchers/used")
    suspend fun markVouchersAsUsed(
        @HeaderMap headers: Map<String, String>,
        @Query(value = MOBILE_NUMBER, encoded = false) mobileNumber: String?,
        @Query(value = ACCOUNT_NUMBER, encoded = false) accountNumber: String?,
        @Body body: MarkVoucherAsUsedRequest,
    ): Response<Unit?>

    @GET("v1/paymentMethods/promoVouchers/used")
    suspend fun retrieveUsedVouchers(
        @HeaderMap headers: Map<String, String>,
        @Query(value = MOBILE_NUMBER, encoded = false) mobileNumber: String?,
        @Query(value = ACCOUNT_NUMBER, encoded = false) accountNumber: String?
    ): Response<RetrieveUsedVouchersResponse>

    @GET("v2/paymentMethods/promoVouchers")
    suspend fun getPromoVouchers(
        @HeaderMap headers: Map<String, String>,
        @Query(value = PAGE_NUMBER, encoded = false) pageNumber: Int,
        @Query(value = PAGE_SIZE, encoded = false) pageSize: Int,
        @Query(value = MOBILE_NUMBER, encoded = false) mobileNumber: String,
    ): Response<PromoVouchersResponse>
}

private const val SUBSCRIBER_ID = "subscriberId"
private const val SUBSCRIBER_TYPE = "subscriberType"
private const val CHANNEL = "channel"
private const val EXPIRY_DATE_FROM = "expiryDateFrom"
private const val EXPIRY_DATE_TO = "expiryDateTo"
private const val OFFSET = "offset"
private const val LIMIT = "limit"

private const val MOBILE_NUMBER = "mobileNumber"
private const val ACCOUNT_NUMBER = "accountNumber"

private const val PAGE_NUMBER = "pageNumber"
private const val PAGE_SIZE = "pageSize"
