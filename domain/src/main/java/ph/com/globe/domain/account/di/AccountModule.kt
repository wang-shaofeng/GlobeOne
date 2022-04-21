/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * UnAccountorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.account.di

import dagger.Module
import dagger.Subcomponent
import ph.com.globe.domain.ManagerScope
import ph.com.globe.domain.account.usecase.*
import ph.com.globe.domain.user_details.usecases.EncryptDataUseCase

@Module(subcomponents = [AccountComponent::class])
internal interface AccountModule

@ManagerScope
@Subcomponent
interface AccountComponent {

    fun provideFetchOcsTokenUseCase(): FetchOcsAccessTokenUseCase

    fun provideGetAccountBrandUseCase(): GetAccountBrandUseCase

    fun provideGetAccountStatusUseCase(): GetAccountStatusUseCase

    fun provideGetAccountDetailsUseCase(): GetAccountDetailsUseCase

    fun provideGetMobilePlanDetailsUseCase(): GetMobilePlanDetailsUseCase

    fun provideGetBroadbandPlanDetailsUseCase(): GetBroadbandPlanDetailsUseCase

    fun provideGetMigratedAccountsUseCase(): GetMigratedAccountsUseCase

    fun provideInquirePrepaidBalanceUseCase(): InquirePrepaidBalanceUseCase

    fun provideGetPrepaidPromoSubscriptionUsageUseCase(): GetPrepaidPromoSubscriptionUsageUseCase

    fun provideGetPrepaidPromoActiveSubscriptionUseCase(): GetPrepaidPromoActiveSubscriptionUseCase

    fun provideGetPostpaidPromoSubscriptionUsageUseCase(): GetPostpaidPromoSubscriptionUsageUseCase

    fun provideGetPostpaidActivePromoSubscriptionUseCase(): GetPostpaidActivePromoSubscriptionUseCase

    fun provideGetAccountAccessTypeUseCase(): GetAccountAccessTypeUseCase

    fun provideGetUsageConsumptionReportsUseCase(): GetUsageConsumptionReportsUseCase

    fun provideEnrollAccountsUseCase(): EnrollAccountsUseCase

    fun provideModifyEnrolledAccountUseCase(): ModifyEnrolledAccountUseCase

    fun provideDeleteEnrolledAccountUseCase(): DeleteEnrolledAccountUseCase

    fun provideEnrollMigratedAccountsUseCase(): EnrollMigratedAccountsUseCase

    fun provideGetCustomerCampaignPromoUseCase(): GetCustomerCampaignPromoUseCase

    fun providePurchaseCampaignPromoUseCase(): PurchaseCampaignPromoUseCase

    fun provideGetPersistentBrandsUseCase(): GetPersistentBrandsUseCase

    fun provideStoreBrandsUseCase(): StoreBrandsUseCase

    fun provideGetAccountsLoadingStateUseCase(): GetAccountsLoadingStateUseCase

    fun provideSetAccountsLoadingStateUseCase(): SetAccountsLoadingStateUseCase

    fun provideGetAccountDataUsageItemsUseCase(): GetAccountDataUsageItemsUseCase

    fun provideGetPostpaidAccountDataUsageItemsUseCase(): GetPostpaidAccountDataUsageItemsUseCase

    fun provideGetEncryptedEmailUseCase(): EncryptDataUseCase

    @Subcomponent.Factory
    interface Factory {
        fun create(): AccountComponent
    }
}
