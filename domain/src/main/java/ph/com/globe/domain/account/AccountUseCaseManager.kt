/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.account

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ph.com.globe.domain.account.di.AccountComponent
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
import javax.inject.Inject

class AccountUseCaseManager @Inject constructor(
    factory: AccountComponent.Factory
) : AccountDomainManager {

    private val accountComponent: AccountComponent = factory.create()

    override suspend fun fetchOcsToken(): LfResult<Unit, GetOcsAccessTokenError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideFetchOcsTokenUseCase().execute()
        }

    override suspend fun getAccountBrand(params: GetAccountBrandParams): LfResult<GetAccountBrandResponse, GetAccountBrandError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetAccountBrandUseCase().execute(params)
        }

    override suspend fun getAccountStatus(params: GetAccountStatusParams): LfResult<GetAccountStatusResult, GetAccountStatusError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetAccountStatusUseCase().execute(params)
        }

    override suspend fun getAccountDetails(params: GetAccountDetailsParams): LfResult<GetAccountDetailsResult, GetAccountDetailsError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetAccountDetailsUseCase().execute(params)
        }

    override suspend fun getMobilePlanDetails(params: GetPlanDetailsParams): LfResult<GetMobilePlanDetailsResult, GetPlanDetailsError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetMobilePlanDetailsUseCase().execute(params)
        }

    override suspend fun getBroadbandPlanDetails(params: GetPlanDetailsParams): LfResult<GetBroadbandPlanDetailsResult, GetPlanDetailsError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetBroadbandPlanDetailsUseCase().execute(params)
        }

    override suspend fun getMigratedAccounts(params: GetMigratedAccountsParams): LfResult<List<EnrolledAccount>, GetEnrolledAccountsError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetMigratedAccountsUseCase().execute(params)
        }

    override suspend fun inquirePrepaidBalance(msisdn: String): LfResult<InquirePrepaidBalanceResult, InquirePrepaidBalanceError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideInquirePrepaidBalanceUseCase().execute(msisdn)
        }

    override suspend fun getPrepaidPromoSubscriptionUsage(request: GetPrepaidPromoSubscriptionUsageRequest): LfResult<PromoSubscriptionUsageResult, GetPrepaidPromoSubscriptionUsageError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetPrepaidPromoSubscriptionUsageUseCase().execute(request)
        }

    override suspend fun getPrepaidPromoActiveSubscription(request: GetPrepaidPromoActiveSubscriptionRequest): LfResult<GetPrepaidPromoActiveSubscriptionResponse, GetPrepaidPromoActiveSubscriptionError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetPrepaidPromoActiveSubscriptionUseCase().execute(request)
        }

    override suspend fun getPostpaidPromoSubscriptionUsage(request: GetPostpaidPromoSubscriptionUsageRequest): LfResult<PromoSubscriptionUsageResult, GetPostpaidPromoSubscriptionUsageError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetPostpaidPromoSubscriptionUsageUseCase().execute(request)
        }

    override suspend fun getPostpaidActivePromoSubscription(request: GetPostpaidActivePromoSubscriptionRequest): LfResult<GetPostpaidActivePromoSubscriptionResponse, GetPostpaidActivePromoSubscriptionError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetPostpaidActivePromoSubscriptionUseCase().execute(request)
        }

    override suspend fun getAccountAccessType(params: GetAccountAccessTypeParams): LfResult<GetAccountAccessTypeResult, GetAccountAccessTypeError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetAccountAccessTypeUseCase().execute(params)
        }

    override suspend fun getUsageConsumptionReports(params: GetUsageConsumptionReportsParams): LfResult<UsageConsumptionResult, GetUsageConsumptionReportsError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetUsageConsumptionReportsUseCase().execute(params)
        }

    override suspend fun enrollAccounts(params: EnrollAccountParams): LfResult<Unit, EnrollAccountsError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideEnrollAccountsUseCase().execute(params)
        }

    override suspend fun modifyEnrolledAccount(
        accountAlias: String,
        modifiedAccount: ModifyEnrolledAccountRequest
    ): LfResult<Unit, ModifyEnrolledAccountError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideModifyEnrolledAccountUseCase()
                .execute(accountAlias, modifiedAccount)
        }

    override suspend fun deleteEnrolledAccount(accountAlias: String): LfResult<Unit, DeleteEnrolledAccountError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideDeleteEnrolledAccountUseCase().execute(accountAlias)
        }

    override suspend fun enrollMigratedAccounts(params: EnrollMigratedAccountsParams): LfResult<EnrollMigratedAccountsResponse, EnrollMigratedAccountsError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideEnrollMigratedAccountsUseCase().execute(params)
        }

    override suspend fun getCustomerCampaignPromo(
        phoneNumber: String,
        segment: String,
        channels: List<PersonalizedCampaignConfig>
    ): LfResult<List<AvailableCampaignPromosModel>, GetCustomerCampaignPromoError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetCustomerCampaignPromoUseCase()
                .execute(phoneNumber, segment, channels)
        }

    override suspend fun purchaseCampaignPromo(
        channel: String,
        mobileNumber: String,
        customParam1: String,
        maId: String,
        mode: Int
    ): LfResult<PurchaseResult, PurchaseCampaignPromoError> = withContext(Dispatchers.IO) {
        accountComponent.providePurchaseCampaignPromoUseCase()
            .execute(channel, mobileNumber, customParam1, maId, mode)
    }

    override fun getPersistentBrands(): Flow<List<PersistentBrandModel>> =
        accountComponent.provideGetPersistentBrandsUseCase().execute()

    override suspend fun storeBrands(brands: List<PersistentBrandModel>) =
        accountComponent.provideStoreBrandsUseCase().execute(brands)

    override fun getAccountsLoadingState(): Flow<AccountsLoadingState> =
        accountComponent.provideGetAccountsLoadingStateUseCase().execute()

    override suspend fun setAccountsLoadingState(state: AccountsLoadingState) =
        accountComponent.provideSetAccountsLoadingStateUseCase().execute(state)

    override suspend fun getAccountDataUsageItems(params: AccountDetailsGroupsParams): Flow<LfResult<List<UsageItem>, AccountDetailsGroupsError>> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetAccountDataUsageItemsUseCase().execute(params)
        }

    override suspend fun getPostpaidAccountDataUsageItems(params: AccountDetailsGroupsParams): Flow<LfResult<List<UsageItem>, AccountDetailsGroupsError>> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetPostpaidAccountDataUsageItemsUseCase().execute(params)
        }
}
