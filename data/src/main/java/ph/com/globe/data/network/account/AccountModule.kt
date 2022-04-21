/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.account

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import ph.com.globe.data.DataScope
import ph.com.globe.data.ManagerScope
import ph.com.globe.data.network.GLOBE_SERVER
import ph.com.globe.data.network.OCS_SERVER
import ph.com.globe.data.network.account.calls.*
import ph.com.globe.data.network.account.repositories.DefaultAccountRepository
import ph.com.globe.data.network.account.repositories.AccountRepository
import retrofit2.Retrofit
import javax.inject.Named

@Module(subcomponents = [AccountComponent::class])
internal interface AccountModule {
    @Binds
    @DataScope
    fun bindAccountRepository(accountRepository: DefaultAccountRepository): AccountRepository
}

@ManagerScope
@Subcomponent(modules = [AccountProvidesModule::class])
interface AccountComponent {

    fun provideGetOcsAccessTokenNetworkCall(): GetOcsAccessTokenNetworkCall

    fun provideGetAccountBrandNetworkCall(): GetAccountBrandNetworkCall

    fun provideGetAccountStatusNetworkCall(): GetAccountStatusNetworkCall

    fun provideGetAccountDetailsNetworkCall(): GetAccountDetailsNetworkCall

    fun provideGetMobilePlanDetailsNetworkCall(): GetMobilePlanDetailsNetworkCall

    fun provideGetBroadbandPlanDetailsNetworkCall(): GetBroadbandPlanDetailsNetworkCall

    fun provideGetMigratedAccountsNetworkCall(): GetMigratedAccountsNetworkCall

    fun provideInquirePrepaidBalanceNetworkCall(): InquirePrepaidBalanceNetworkCall

    fun provideGetPrepaidPromoSubscriptionUsageNetworkCall(): GetPrepaidPromoSubscriptionUsageNetworkCall

    fun provideGetPrepaidPromoActiveSubscriptionNetworkCall(): GetPrepaidPromoActiveSubscriptionNetworkCall

    fun provideGetPostpaidPromoSubscriptionUsageNetworkCall(): GetPostpaidPromoSubscriptionUsageNetworkCall

    fun provideGetPostpaidActivePromoSubscriptionNetworkCall(): GetPostpaidActivePromoSubscriptionNetworkCall

    fun provideGetAccountAccessTypeNetworkCall(): GetAccountAccessTypeNetworkCall

    fun provideGetUsageConsumptionReportsNetworkCall(): GetUsageConsumptionReportsNetworkCall

    fun provideEnrollAccountsNetworkCAll(): EnrollAccountsNetworkCall

    fun provideModifyEnrolledAccountNetworkCall(): ModifyEnrolledAccountNetworkCall

    fun provideDeleteEnrolledAccountNetworkCall(): DeleteEnrolledAccountNetworkCall

    fun provideEnrollMigratedAccountsNetworkCall(): EnrollMigratedAccountsNetworkCall

    fun provideGetCustomerCampaignPromoNetworkCall(): GetCustomerCampaignPromoNetworkCall

    fun providePurchaseCampaignPromoNetworkCall(): PurchaseCampaignPromoNetworkCall

    fun provideAccountRepository(): AccountRepository

    @Subcomponent.Factory
    interface Factory {
        fun create(): AccountComponent
    }
}

@Module
internal object AccountProvidesModule {

    @Provides
    @ManagerScope
    fun providesAccountRetrofit(@Named(GLOBE_SERVER) retrofit: Retrofit): AccountRetrofit =
        retrofit.create(AccountRetrofit::class.java)

    @Provides
    @ManagerScope
    fun providesAccountOcsRetrofit(@Named(OCS_SERVER) retrofit: Retrofit): AccountOcsRetrofit =
        retrofit.create(AccountOcsRetrofit::class.java)
}
