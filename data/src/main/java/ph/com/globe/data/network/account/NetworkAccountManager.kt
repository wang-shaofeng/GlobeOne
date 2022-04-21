/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.account

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.domain.account.AccountDataManager
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.account.*
import ph.com.globe.errors.profile.GetEnrolledAccountsError
import ph.com.globe.model.account.*
import ph.com.globe.model.account.network_models.GetPrepaidPromoSubscriptionUsageParams
import ph.com.globe.model.account.network_models.GetPrepaidPromoSubscriptionUsageResponse
import ph.com.globe.model.payment.PurchaseResult
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.util.LfResult
import javax.inject.Inject

class NetworkAccountManager @Inject constructor(
    factory: AccountComponent.Factory,
    private val tokenRepository: TokenRepository,
) : AccountDataManager {

    private val accountComponent: AccountComponent = factory.create()

    override suspend fun fetchOcsAccessToken(): LfResult<String, GetOcsAccessTokenError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetOcsAccessTokenNetworkCall().execute()
        }

    override suspend fun getOcsAccessToken(): LfResult<String, NetworkError.NoOcsToken> =
        tokenRepository.getOcsToken()

    override suspend fun getAccountBrand(params: GetAccountBrandParams): LfResult<GetAccountBrandResponse, GetAccountBrandError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetAccountBrandNetworkCall().execute(params)
        }

    override suspend fun getAccountStatus(params: GetAccountStatusParams): LfResult<GetAccountStatusResult, GetAccountStatusError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetAccountStatusNetworkCall().execute(params)
        }

    override suspend fun getAccountDetails(params: GetAccountDetailsParams): LfResult<GetAccountDetailsResult, GetAccountDetailsError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetAccountDetailsNetworkCall().execute(params)
        }

    override suspend fun getMobilePlanDetails(params: GetPlanDetailsParams): LfResult<GetMobilePlanDetailsResult, GetPlanDetailsError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetMobilePlanDetailsNetworkCall().execute(params)
        }

    override suspend fun getBroadbandPlanDetails(params: GetPlanDetailsParams): LfResult<GetBroadbandPlanDetailsResult, GetPlanDetailsError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetBroadbandPlanDetailsNetworkCall().execute(params)
        }

    override suspend fun getMigratedAccounts(params: GetMigratedAccountsParams): LfResult<List<EnrolledAccount>, GetEnrolledAccountsError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetMigratedAccountsNetworkCall().execute(params)
        }

    override suspend fun inquirePrepaidBalance(msisdn: String): LfResult<InquirePrepaidBalanceResult, InquirePrepaidBalanceError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideInquirePrepaidBalanceNetworkCall().execute(msisdn)
        }

    override suspend fun setOcsToken(token: String) = tokenRepository.setOcsToken(token)

    override suspend fun getPrepaidPromoSubscriptionUsage(
        params: GetPrepaidPromoSubscriptionUsageParams
    ): LfResult<GetPrepaidPromoSubscriptionUsageResponse, GetPrepaidPromoSubscriptionUsageError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetPrepaidPromoSubscriptionUsageNetworkCall().execute(params)
        }

    override suspend fun getPrepaidPromoActiveSubscription(
        token: String,
        request: GetPrepaidPromoActiveSubscriptionRequest
    ): LfResult<GetPrepaidPromoActiveSubscriptionResponse, GetPrepaidPromoActiveSubscriptionError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetPrepaidPromoActiveSubscriptionNetworkCall()
                .execute(token, request)
        }

    override suspend fun getPostpaidPromoSubscriptionUsage(
        token: String,
        request: GetPostpaidPromoSubscriptionUsageRequest
    ): LfResult<GetPostpaidPromoSubscriptionUsageResponse, GetPostpaidPromoSubscriptionUsageError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetPostpaidPromoSubscriptionUsageNetworkCall()
                .execute(token, request)
        }

    override suspend fun getPostpaidActivePromoSubscription(
        token: String,
        request: GetPostpaidActivePromoSubscriptionRequest
    ): LfResult<GetPostpaidActivePromoSubscriptionResponse, GetPostpaidActivePromoSubscriptionError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetPostpaidActivePromoSubscriptionNetworkCall()
                .execute(token, request)
        }

    override suspend fun getAccountAccessType(params: GetAccountAccessTypeParams): LfResult<GetAccountAccessTypeResult, GetAccountAccessTypeError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetAccountAccessTypeNetworkCall().execute(params)
        }

    override suspend fun getUsageConsumptionReports(params: GetUsageConsumptionReportsParams): LfResult<UsageConsumptionResult, GetUsageConsumptionReportsError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetUsageConsumptionReportsNetworkCall().execute(params)
        }

    override suspend fun enrollAccounts(params: EnrollAccountParams): LfResult<Unit, EnrollAccountsError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideEnrollAccountsNetworkCAll().execute(params)
        }

    override suspend fun modifyEnrolledAccount(
        accountAlias: String,
        modifiedAccount: ModifyEnrolledAccountRequest
    ): LfResult<Unit, ModifyEnrolledAccountError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideModifyEnrolledAccountNetworkCall()
                .execute(accountAlias, modifiedAccount)
        }

    override suspend fun deleteEnrolledAccount(accountAlias: String): LfResult<Unit, DeleteEnrolledAccountError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideDeleteEnrolledAccountNetworkCall().execute(accountAlias)
        }

    override suspend fun enrollMigratedAccounts(params: EnrollMigratedAccountsParams): LfResult<EnrollMigratedAccountsResponse, EnrollMigratedAccountsError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideEnrollMigratedAccountsNetworkCall().execute(params)
        }

    override suspend fun getCustomerCampaignPromo(
        params: GetCustomerCampaignParams
    ): LfResult<List<AvailableCampaignPromosModel>, GetCustomerCampaignPromoError> =
        withContext(Dispatchers.IO) {
            accountComponent.provideGetCustomerCampaignPromoNetworkCall()
                .execute(params)
        }

    override suspend fun purchaseCampaignPromo(
        channel: String,
        mobileNumber: String,
        customParam1: String,
        maId: String,
        availMode: Int
    ): LfResult<PurchaseResult, PurchaseCampaignPromoError> = withContext(Dispatchers.IO) {
        accountComponent.providePurchaseCampaignPromoNetworkCall()
            .execute(channel, mobileNumber, customParam1, maId, availMode)
    }

    override suspend fun removeOcsToken() =
        tokenRepository.removeOcsToken()

    override fun getPersistentBrands(): Flow<List<PersistentBrandModel>> =
        accountComponent.provideAccountRepository().getPersistentBrands()

    override suspend fun storeBrands(brands: List<PersistentBrandModel>) =
        accountComponent.provideAccountRepository().storeBrands(brands)

    override fun getAccountsLoadingState(): Flow<AccountsLoadingState> =
        accountComponent.provideAccountRepository().getAccountsLoadingState()

    override suspend fun setAccountsLoadingState(state: AccountsLoadingState) =
        accountComponent.provideAccountRepository().setAccountsLoadingState(state)
}
