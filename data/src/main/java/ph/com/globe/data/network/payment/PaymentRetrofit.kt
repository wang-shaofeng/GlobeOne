/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.payment

import okhttp3.RequestBody
import okhttp3.ResponseBody
import ph.com.globe.data.network.payment.model.*
import ph.com.globe.model.payment.*
import retrofit2.Response
import retrofit2.http.*

interface PaymentRetrofit {

    @POST("v1/paymentManagement/payments/sessions")
    suspend fun createAdyenPaymentSession(
        @HeaderMap header: Map<String, String>,
        @Body request: CreateAdyenPaymentSessionRequest
    ): Response<CreatePaymentSessionResponse>

    @POST("v1/paymentManagement/payments/sessions")
    suspend fun createGCashPaymentSession(
        @HeaderMap header: Map<String, String>,
        @Body request: CreateGCashPaymentSessionRequest
    ): Response<CreatePaymentSessionResponse>

    @GET("v1/accountManagement/gcash/info")
    suspend fun getGCashAccountInfo(
        @HeaderMap headers: Map<String, String>,
        @Query("mobileNumber") mobileNumber: String
    ): Response<GetGCashAccountInfoResponse>

    @GET("v2/partners/gcash/accounts/{mobileNumber}/balance")
    suspend fun getGCashBalance(
        @HeaderMap headers: Map<String, String>,
        @Path(value = "mobileNumber", encoded = false) mobileNumber: String
    ): Response<GetGCashBalanceResponse>

    @GET("v1/paymentManagement/payments/{tokenPaymentId}/sessions")
    suspend fun getPaymentSessionByTokenId(
        @HeaderMap headers: Map<String, String>,
        @Path(value = "tokenPaymentId", encoded = false) tokenPaymentId: String
    ): Response<GetPaymentSessionResponse>

    @POST("/v1/paymentManagement/services")
    suspend fun paymentService(
        @HeaderMap headers: Map<String, String>,
        @Body request: RequestBody
    ): Response<PaymentServiceResponse>

    @GET("v1/paymentMethods/{userUUID}")
    suspend fun getPaymentMethod(
        @HeaderMap headers: Map<String, String>,
        @Path(value = "userUUID", encoded = false) userUUID: String,
        @Query("financialAccountType") financialAccountType: Int = 1
    ): Response<GetPaymentMethodResponse>

    @DELETE("v1/paymentMethods/{financialAccountId}")
    suspend fun deletePaymentMethod(
        @HeaderMap headers: Map<String, String>,
        @Path(value = "financialAccountId", encoded = false) financialAccountId: String,
        @Query("cardReference") cardReference: String
    ): Response<Unit?>

    @POST("v2/partners/gcash/link")
    suspend fun linkGCashAccount(
        @HeaderMap headers: Map<String, String>,
        @Body accountAlias: LinkingGCashAccountRequest
    ): Response<Unit?>

    @POST("v2/partners/gcash/unlink")
    suspend fun unlinkGCashAccount(
        @HeaderMap headers: Map<String, String>,
        @Body accountAlias: LinkingGCashAccountRequest
    ): Response<Unit?>

    @POST("v1/productOrdering")
    suspend fun purchasePromoByServiceId(
        @HeaderMap headers: Map<String, String>,
        @Body request: PurchasePromoRequest
    ): Response<Unit?>

    @POST("v2/productOrdering/promos")
    suspend fun multiplePurchasePromo(
        @HeaderMap headers: Map<String, String>,
        @Body request: MultiplePurchasePromoRequest
    ): Response<MultiplePurchasePromoResponse>

    @POST("v1/balanceManagement/customers/{msisdn}/topUp")
    suspend fun topUpConsumer(
        @HeaderMap headers: Map<String, String>,
        @Path(value = "msisdn", encoded = false) msisdn: String,
        @Body request: PurchaseLoadConsumerRequest
    ): Response<PurchaseLoadResponse>

    @POST("v1/balanceManagement/customers/{retail_msisdn}/topUp")
    suspend fun topUpRetailer(
        @HeaderMap headers: Map<String, String>,
        @Path(value = "retail_msisdn", encoded = false) msisdn: String,
        @Body request: PurchaseLoadRetailerRequest
    ): Response<PurchaseLoadResponse>

    @POST("v1/serviceOrdering")
    suspend fun createServiceOrderLoad(
        @HeaderMap headers: Map<String, String>,
        @Body request: CreateServiceOrderLoadRequest
    ): Response<Unit?>

    @POST("v1/serviceOrdering")
    suspend fun createServiceOrderPromo(
        @HeaderMap headers: Map<String, String>,
        @Body request: CreateServiceOrderPromoRequest
    ): Response<Unit?>

    @Streaming
    @GET("/v1/paymentManagement/receipts/{receiptId}")
    suspend fun getPaymentReceipt(
        @HeaderMap headers: Map<String, String>,
        @Path("receiptId") receiptId: String,
        @Query("storeId") storeId: String? = "60001"
    ): Response<ResponseBody>

    @GET("/v1/paymentManagement/payments")
    suspend fun getPayments(
        @HeaderMap headers: Map<String, String>,
        @Query("mobileNumber") mobileNumber: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<GetPaymentsResponse>
}
