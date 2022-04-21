/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.account

import ph.com.globe.model.account.*
import retrofit2.Response
import retrofit2.http.*

interface AccountRetrofit {

    @GET("v1/accountManagement/accounts/brand")
    suspend fun getAccountBrand(
        @QueryMap query: Map<String, String>
    ): Response<GetAccountBrandResponse>

    @GET("v1/accountManagement/accounts/status")
    suspend fun getAccountStatus(
        @HeaderMap headers: Map<String, String>,
        @QueryMap query: Map<String, String>
    ): Response<GetAccountStatusResponse>

    @GET("/v2/accountManagement/accounts")
    suspend fun getAccountDetails(
        @HeaderMap headers: Map<String, String>,
        @QueryMap query: Map<String, String>
    ): Response<GetAccountDetailsResponse>

    @GET("/v3/accountManagement/accounts/products/plans")
    suspend fun getMobilePlanDetails(
        @HeaderMap headers: Map<String, String>,
        @QueryMap query: Map<String, String>
    ): Response<GetMobilePlanDetailsResponse>

    @GET("/v3/accountManagement/accounts/products/plans")
    suspend fun getBroadbandPlanDetails(
        @HeaderMap headers: Map<String, String>,
        @QueryMap query: Map<String, String>
    ): Response<GetBroadbandPlanDetailsResponse>

    @GET("v2/balanceManagement/{msisdn}/balance")
    suspend fun inquirePrepaidBalance(
        @HeaderMap headers: Map<String, String>,
        @Path("msisdn") msisdn: String
    ): Response<InquirePrepaidBalanceResponse>

    @GET("v1/userManagement/migrations/accounts")
    suspend fun getMigratedAccounts(
        @HeaderMap headers: Map<String, String>,
        @QueryMap query: Map<String, String>
    ): Response<GetMigratedAccountsResponse>

    @POST("v2/userManagement/accounts")
    suspend fun enrollAccounts(
        @HeaderMap headers: Map<String, String>,
        @Body enrollAccountsJsonBodyRequest: EnrollAccountRequest
    ): Response<Unit?>

    @POST("v2/userManagement/migrations/accounts")
    suspend fun enrollMigratedAccounts(
        @HeaderMap headers: Map<String, String>,
        @Body enrollAccounts: EnrollMigratedAccountsParams
    ): Response<EnrollMigratedAccountsResponse>

    @PUT("v2/userManagement/accounts")
    suspend fun modifyEnrolledAccount(
        @HeaderMap headers: Map<String, String>,
        @Query("accountAlias") accountAlias: String,
        @Body enrolledAccount: ModifyEnrolledAccountRequest
    ): Response<Unit?>

    @DELETE("v1/userManagement/accounts")
    suspend fun deleteEnrolledAccount(
        @HeaderMap headers: Map<String, String>,
        @Query("accountAlias") accountAlias: String,
    ): Response<Unit?>

    @GET("v2/productOrdering/campaigns")
    suspend fun getCustomerCampaignPromo(
        @HeaderMap headers: Map<String, String>,
        @QueryMap query: Map<String, String>
    ): Response<GetCustomerCampaignPromoResponse>

    @POST("v2/productOrdering/campaigns")
    suspend fun purchaseCampaignPromo(
        @HeaderMap headers: Map<String, String>,
        @Body campaignPromo: CampaignPromoRequestModel,
        @Query("mode") mode: Int = 1
    ): Response<CampaignPromoResponseModel>

    @GET("v2/accountManagement/accounts/accessType")
    suspend fun getAccountAccessType(
        @HeaderMap headers: Map<String, String>,
        @QueryMap params: Map<String, String>,
    ): Response<GetAccountAccessTypeResponse>

    @GET("v2/usageConsumption/reports")
    suspend fun getUsageConsumptionReports(
        @HeaderMap headers: Map<String, String>,
        @QueryMap params: Map<String, String>,
    ): Response<GetUsageConsumptionReportsResponse>
}
