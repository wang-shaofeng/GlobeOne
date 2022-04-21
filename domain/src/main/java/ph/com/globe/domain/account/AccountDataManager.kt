/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.account

import kotlinx.coroutines.flow.Flow
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.account.*
import ph.com.globe.errors.profile.GetEnrolledAccountsError
import ph.com.globe.model.account.*
import ph.com.globe.model.account.network_models.GetPrepaidPromoSubscriptionUsageParams
import ph.com.globe.model.account.network_models.GetPrepaidPromoSubscriptionUsageResponse
import ph.com.globe.model.payment.PurchaseResult
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.util.LfResult

interface AccountDataManager {

    suspend fun fetchOcsAccessToken(): LfResult<String, GetOcsAccessTokenError>

    suspend fun getOcsAccessToken(): LfResult<String, NetworkError.NoOcsToken>

    suspend fun getAccountBrand(params: GetAccountBrandParams): LfResult<GetAccountBrandResponse, GetAccountBrandError>

    suspend fun getAccountStatus(params: GetAccountStatusParams): LfResult<GetAccountStatusResult, GetAccountStatusError>

    suspend fun getAccountDetails(params: GetAccountDetailsParams): LfResult<GetAccountDetailsResult, GetAccountDetailsError>

    suspend fun getMobilePlanDetails(params: GetPlanDetailsParams): LfResult<GetMobilePlanDetailsResult, GetPlanDetailsError>

    suspend fun getBroadbandPlanDetails(params: GetPlanDetailsParams): LfResult<GetBroadbandPlanDetailsResult, GetPlanDetailsError>

    suspend fun getMigratedAccounts(params: GetMigratedAccountsParams): LfResult<List<EnrolledAccount>, GetEnrolledAccountsError>

    suspend fun inquirePrepaidBalance(msisdn: String): LfResult<InquirePrepaidBalanceResult, InquirePrepaidBalanceError>

    suspend fun setOcsToken(token: String)

    suspend fun getPrepaidPromoSubscriptionUsage(
        params: GetPrepaidPromoSubscriptionUsageParams
    ): LfResult<GetPrepaidPromoSubscriptionUsageResponse, GetPrepaidPromoSubscriptionUsageError>

    suspend fun getPrepaidPromoActiveSubscription(
        token: String,
        request: GetPrepaidPromoActiveSubscriptionRequest
    ): LfResult<GetPrepaidPromoActiveSubscriptionResponse, GetPrepaidPromoActiveSubscriptionError>

    suspend fun getPostpaidPromoSubscriptionUsage(
        token: String,
        request: GetPostpaidPromoSubscriptionUsageRequest
    ): LfResult<GetPostpaidPromoSubscriptionUsageResponse, GetPostpaidPromoSubscriptionUsageError>

    suspend fun getPostpaidActivePromoSubscription(
        token: String,
        request: GetPostpaidActivePromoSubscriptionRequest
    ): LfResult<GetPostpaidActivePromoSubscriptionResponse, GetPostpaidActivePromoSubscriptionError>

    suspend fun getAccountAccessType(params: GetAccountAccessTypeParams): LfResult<GetAccountAccessTypeResult, GetAccountAccessTypeError>

    suspend fun getUsageConsumptionReports(params: GetUsageConsumptionReportsParams): LfResult<UsageConsumptionResult, GetUsageConsumptionReportsError>

    suspend fun enrollAccounts(params: EnrollAccountParams): LfResult<Unit, EnrollAccountsError>

    suspend fun modifyEnrolledAccount(
        accountAlias: String,
        modifiedAccount: ModifyEnrolledAccountRequest
    ): LfResult<Unit, ModifyEnrolledAccountError>

    suspend fun deleteEnrolledAccount(accountAlias: String): LfResult<Unit, DeleteEnrolledAccountError>

    suspend fun enrollMigratedAccounts(params: EnrollMigratedAccountsParams): LfResult<EnrollMigratedAccountsResponse, EnrollMigratedAccountsError>

    suspend fun getCustomerCampaignPromo(
        params: GetCustomerCampaignParams
    ): LfResult<List<AvailableCampaignPromosModel>, GetCustomerCampaignPromoError>

    suspend fun purchaseCampaignPromo(
        channel: String,
        mobileNumber: String,
        customParam1: String,
        maId: String,
        availMode: Int
    ): LfResult<PurchaseResult, PurchaseCampaignPromoError>

    suspend fun removeOcsToken()

    fun getPersistentBrands(): Flow<List<PersistentBrandModel>>

    suspend fun storeBrands(brands: List<PersistentBrandModel>)

    fun getAccountsLoadingState(): Flow<AccountsLoadingState>

    suspend fun setAccountsLoadingState(state: AccountsLoadingState)
}
