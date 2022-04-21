/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ph.com.globe.domain.UseCaseComponent
import ph.com.globe.domain.account.AccountDomainManager
import ph.com.globe.domain.account_activities.AccountActivitiesDomainManager
import ph.com.globe.domain.app_data.AppDataDomainManager
import ph.com.globe.domain.auth.AuthDomainManager
import ph.com.globe.domain.balance.BalanceDomainManager
import ph.com.globe.domain.banners.BannersDomainManager
import ph.com.globe.domain.billings.BillingsDomainManager
import ph.com.globe.domain.catalog.CatalogDomainManager
import ph.com.globe.domain.connectivity.ConnectivityDomainManager
import ph.com.globe.domain.credit.CreditDomainManager
import ph.com.globe.domain.database.DatabaseDomainManager
import ph.com.globe.domain.group.GroupDomainManager
import ph.com.globe.domain.maintenance.MaintenanceDomainManager
import ph.com.globe.domain.payment.PaymentDomainManager
import ph.com.globe.domain.prepaid.PrepaidDomainManager
import ph.com.globe.domain.profile.ProfileDomainManager
import ph.com.globe.domain.rewards.RewardsDomainManager
import ph.com.globe.domain.rush.RushDomainManager
import ph.com.globe.domain.session.SessionDomainManager
import ph.com.globe.domain.shop.ShopDomainManager
import ph.com.globe.domain.user_details.UserDetailsDomainManager
import ph.com.globe.domain.voucher.VoucherDomainManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseManagersModule {

    @Singleton
    @Provides
    fun provideAuthDomainManager(useCaseComponent: UseCaseComponent): AuthDomainManager =
        useCaseComponent.provideAuthDomainManager()

    @Singleton
    @Provides
    fun provideProfileDomainManager(useCaseComponent: UseCaseComponent): ProfileDomainManager =
        useCaseComponent.provideProfileDomainManager()

    @Singleton
    @Provides
    fun provideBalanceDomainManager(useCaseComponent: UseCaseComponent): BalanceDomainManager =
        useCaseComponent.provideBalanceDomainManager()

    @Singleton
    @Provides
    fun provideAccountDomainManager(useCaseComponent: UseCaseComponent): AccountDomainManager =
        useCaseComponent.provideAccountDomainManager()

    @Singleton
    @Provides
    fun providePaymentDomainManager(useCaseComponent: UseCaseComponent): PaymentDomainManager =
        useCaseComponent.providePaymentDomainManager()

    @Singleton
    @Provides
    fun provideRewardsDomainManager(useCaseComponent: UseCaseComponent): RewardsDomainManager =
        useCaseComponent.provideRewardsDomainManager()

    @Singleton
    @Provides
    fun provideCreditDomainManager(useCaseComponent: UseCaseComponent): CreditDomainManager =
        useCaseComponent.provideCreditDomainManager()

    @Singleton
    @Provides
    fun provideShopDomainManager(useCaseComponent: UseCaseComponent): ShopDomainManager =
        useCaseComponent.provideShopDomainManager()

    @Singleton
    @Provides
    fun provideGroupDomainManager(useCaseComponent: UseCaseComponent): GroupDomainManager =
        useCaseComponent.provideGroupDomainManager()

    @Singleton
    @Provides
    fun provideUserDetailsManager(useCaseComponent: UseCaseComponent): UserDetailsDomainManager =
        useCaseComponent.provideUserDetailsDomainManager()

    @Singleton
    @Provides
    fun provideCatalogDomainManager(useCaseComponent: UseCaseComponent): CatalogDomainManager =
        useCaseComponent.provideCatalogDomainManager()

    @Singleton
    @Provides
    fun provideSessionDomainManager(useCaseComponent: UseCaseComponent): SessionDomainManager =
        useCaseComponent.provideSessionDomainManager()

    @Singleton
    @Provides
    fun provideConnectivityDomainManager(useCaseComponent: UseCaseComponent): ConnectivityDomainManager =
        useCaseComponent.provideConnectivityDomainManager()

    @Singleton
    @Provides
    fun provideDatabaseDomainManager(useCaseComponent: UseCaseComponent): DatabaseDomainManager =
        useCaseComponent.provideDatabaseDomainManager()

    @Singleton
    @Provides
    fun provideRushDomainManager(useCaseComponent: UseCaseComponent): RushDomainManager =
        useCaseComponent.provideRushDomainManager()

    @Singleton
    @Provides
    fun provideAccountActivitiesDomainManager(useCaseComponent: UseCaseComponent): AccountActivitiesDomainManager =
        useCaseComponent.provideAccountActivitiesDomainManager()

    @Singleton
    @Provides
    fun provideVoucherDomainManager(useCaseComponent: UseCaseComponent): VoucherDomainManager =
        useCaseComponent.provideVoucherDomainManager()

    @Singleton
    @Provides
    fun provideAppDataDomainManager(useCaseComponent: UseCaseComponent): AppDataDomainManager =
        useCaseComponent.provideAppDataDomainManager()

    @Singleton
    @Provides
    fun provideBillingsDomainManager(useCaseComponent: UseCaseComponent): BillingsDomainManager =
        useCaseComponent.provideBillingsDomainManager()

    @Singleton
    @Provides
    fun providePrepaidDomainManager(useCaseComponent: UseCaseComponent): PrepaidDomainManager =
        useCaseComponent.providePrepaidDomainManager()

    @Singleton
    @Provides
    fun provideBannersDomainManager(useCaseComponent: UseCaseComponent): BannersDomainManager =
        useCaseComponent.provideBannersDomainManager()

    @Singleton
    @Provides
    fun provideMaintenanceDomainManager(useCaseComponent: UseCaseComponent): MaintenanceDomainManager =
        useCaseComponent.provideMaintenanceDomainManager()
}
