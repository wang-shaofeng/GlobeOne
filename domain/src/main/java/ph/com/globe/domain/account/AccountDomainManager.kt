/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.account

import kotlinx.coroutines.flow.Flow
import ph.com.globe.errors.account.*
import ph.com.globe.errors.group.AccountDetailsGroupsError
import ph.com.globe.errors.profile.GetEnrolledAccountsError
import ph.com.globe.model.account.*
import ph.com.globe.model.account.network_models.PromoSubscriptionUsageResult
import ph.com.globe.model.account.network_models.GetPrepaidPromoSubscriptionUsageRequest
import ph.com.globe.model.group.domain_models.AccountDetailsGroupsParams
import ph.com.globe.model.group.domain_models.UsageItem
import ph.com.globe.model.payment.PurchaseResult
import ph.com.globe.model.personalized_campaign.PersonalizedCampaignConfig
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.util.LfResult

interface AccountDomainManager {

    suspend fun fetchOcsToken(): LfResult<Unit, GetOcsAccessTokenError>

    suspend fun getAccountBrand(params: GetAccountBrandParams): LfResult<GetAccountBrandResponse, GetAccountBrandError>

    suspend fun getAccountStatus(params: GetAccountStatusParams): LfResult<GetAccountStatusResult, GetAccountStatusError>

    suspend fun getAccountDetails(params: GetAccountDetailsParams): LfResult<GetAccountDetailsResult, GetAccountDetailsError>

    suspend fun getMobilePlanDetails(params: GetPlanDetailsParams): LfResult<GetMobilePlanDetailsResult, GetPlanDetailsError>

    suspend fun getBroadbandPlanDetails(params: GetPlanDetailsParams): LfResult<GetBroadbandPlanDetailsResult, GetPlanDetailsError>

    suspend fun getMigratedAccounts(params: GetMigratedAccountsParams): LfResult<List<EnrolledAccount>, GetEnrolledAccountsError>

    suspend fun inquirePrepaidBalance(msisdn: String): LfResult<InquirePrepaidBalanceResult, InquirePrepaidBalanceError>

    suspend fun getPrepaidPromoSubscriptionUsage(request: GetPrepaidPromoSubscriptionUsageRequest): LfResult<PromoSubscriptionUsageResult, GetPrepaidPromoSubscriptionUsageError>

    suspend fun getPrepaidPromoActiveSubscription(request: GetPrepaidPromoActiveSubscriptionRequest): LfResult<GetPrepaidPromoActiveSubscriptionResponse, GetPrepaidPromoActiveSubscriptionError>

    suspend fun getPostpaidPromoSubscriptionUsage(request: GetPostpaidPromoSubscriptionUsageRequest): LfResult<PromoSubscriptionUsageResult, GetPostpaidPromoSubscriptionUsageError>

    suspend fun getPostpaidActivePromoSubscription(request: GetPostpaidActivePromoSubscriptionRequest): LfResult<GetPostpaidActivePromoSubscriptionResponse, GetPostpaidActivePromoSubscriptionError>

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
        phoneNumber: String,
        segment: String,
        channels: List<PersonalizedCampaignConfig>
    ): LfResult<List<AvailableCampaignPromosModel>, GetCustomerCampaignPromoError>

    suspend fun purchaseCampaignPromo(
        channel: String,
        mobileNumber: String,
        customParam1: String,
        maId: String,
        mode: Int
    ): LfResult<PurchaseResult, PurchaseCampaignPromoError>

    fun getPersistentBrands(): Flow<List<PersistentBrandModel>>

    suspend fun storeBrands(brands: List<PersistentBrandModel>)

    fun getAccountsLoadingState(): Flow<AccountsLoadingState>

    suspend fun setAccountsLoadingState(state: AccountsLoadingState)

    suspend fun getAccountDataUsageItems(params: AccountDetailsGroupsParams): Flow<LfResult<List<UsageItem>, AccountDetailsGroupsError>>

    suspend fun getPostpaidAccountDataUsageItems(params: AccountDetailsGroupsParams): Flow<LfResult<List<UsageItem>, AccountDetailsGroupsError>>
}
