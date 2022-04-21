/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain

import dagger.*
import ph.com.globe.domain.account.AccountDomainManager
import ph.com.globe.domain.account.AccountUseCaseManager
import ph.com.globe.domain.account.di.AccountModule
import ph.com.globe.domain.account_activities.AccountActivitiesDomainManager
import ph.com.globe.domain.account_activities.AccountActivitiesUseCaseManager
import ph.com.globe.domain.account_activities.di.AccountActivitiesModule
import ph.com.globe.domain.app_data.AppDataDomainManager
import ph.com.globe.domain.app_data.AppDataUseCaseManager
import ph.com.globe.domain.app_data.di.AppDataModule
import ph.com.globe.domain.auth.AuthDomainManager
import ph.com.globe.domain.auth.AuthUseCaseManager
import ph.com.globe.domain.auth.di.AuthModule
import ph.com.globe.domain.balance.BalanceDomainManager
import ph.com.globe.domain.balance.BalanceUseCaseManager
import ph.com.globe.domain.balance.di.BalanceModule
import ph.com.globe.domain.banners.BannersDomainManager
import ph.com.globe.domain.banners.BannersUseCaseManager
import ph.com.globe.domain.banners.di.BannersModule
import ph.com.globe.domain.billings.BillingsDomainManager
import ph.com.globe.domain.billings.BillingsUseCaseManager
import ph.com.globe.domain.billings.di.BillingsModule
import ph.com.globe.domain.catalog.CatalogDomainManager
import ph.com.globe.domain.catalog.CatalogUseCaseManager
import ph.com.globe.domain.catalog.di.CatalogModule
import ph.com.globe.domain.connectivity.ConnectivityDomainManager
import ph.com.globe.domain.connectivity.ConnectivityUseCaseManager
import ph.com.globe.domain.connectivity.di.ConnectivityModule
import ph.com.globe.domain.credit.CreditDomainManager
import ph.com.globe.domain.credit.CreditUserCaseManager
import ph.com.globe.domain.credit.di.CreditModule
import ph.com.globe.domain.database.DatabaseDomainManager
import ph.com.globe.domain.database.DatabaseUseCaseManager
import ph.com.globe.domain.database.di.DatabaseModule
import ph.com.globe.domain.group.GroupDomainManager
import ph.com.globe.domain.group.GroupUseCaseManager
import ph.com.globe.domain.group.di.GroupModule
import ph.com.globe.domain.maintenance.MaintenanceDomainManager
import ph.com.globe.domain.maintenance.MaintenanceUseCaseManager
import ph.com.globe.domain.maintenance.di.MaintenanceModule
import ph.com.globe.domain.payment.PaymentDomainManager
import ph.com.globe.domain.payment.PaymentUseCaseManager
import ph.com.globe.domain.payment.di.PaymentModule
import ph.com.globe.domain.prepaid.PrepaidDomainManager
import ph.com.globe.domain.prepaid.PrepaidUseCaseManager
import ph.com.globe.domain.prepaid.di.PrepaidModule
import ph.com.globe.domain.profile.ProfileDomainManager
import ph.com.globe.domain.profile.ProfileUseCaseManager
import ph.com.globe.domain.profile.di.ProfileModule
import ph.com.globe.domain.rewards.RewardsDomainManager
import ph.com.globe.domain.rewards.RewardsUseCaseManager
import ph.com.globe.domain.rewards.di.RewardsModule
import ph.com.globe.domain.rush.RushDomainManager
import ph.com.globe.domain.rush.RushUseCaseManager
import ph.com.globe.domain.rush.di.RushModule
import ph.com.globe.domain.session.SessionDomainManager
import ph.com.globe.domain.session.SessionUseCaseManager
import ph.com.globe.domain.session.di.SessionModule
import ph.com.globe.domain.shop.ShopDomainManager
import ph.com.globe.domain.shop.ShopUseCaseManager
import ph.com.globe.domain.shop.di.ShopModule
import ph.com.globe.domain.user_details.UserDetailsDomainManager
import ph.com.globe.domain.user_details.UserDetailsUseCaseManager
import ph.com.globe.domain.user_details.di.UserDetailsModule
import ph.com.globe.domain.voucher.VoucherDomainManager
import ph.com.globe.domain.voucher.VoucherUseCaseManager
import ph.com.globe.domain.voucher.di.VoucherModule
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention(value = AnnotationRetention.RUNTIME)
internal annotation class UseCaseScope

@Scope
@MustBeDocumented
@Retention(value = AnnotationRetention.RUNTIME)
internal annotation class ManagerScope

/**
 * When adding a new manager, don't forget to add it in [UseCaseManagersModule]!!!
 */

@UseCaseScope
@Component(
    modules = [
        DomainManagersModule::class, DataManagersModule::class, AuthModule::class, SessionModule::class,
        ProfileModule::class, AccountModule::class, BalanceModule::class, PaymentModule::class, RewardsModule::class,
        CreditModule::class, ShopModule::class, GroupModule::class, UserDetailsModule::class, CatalogModule::class,
        ConnectivityModule::class, DatabaseModule::class, RushModule::class, AccountActivitiesModule::class,
        AccountActivitiesModule::class, PrepaidModule::class, VoucherModule::class, AppDataModule::class, BillingsModule::class,
        BannersModule::class, MaintenanceModule::class
    ]
)
interface UseCaseComponent {

    fun provideAuthDomainManager(): AuthDomainManager

    fun provideProfileDomainManager(): ProfileDomainManager

    fun provideBalanceDomainManager(): BalanceDomainManager

    fun provideAccountDomainManager(): AccountDomainManager

    fun providePaymentDomainManager(): PaymentDomainManager

    fun provideRewardsDomainManager(): RewardsDomainManager

    fun provideCreditDomainManager(): CreditDomainManager

    fun provideShopDomainManager(): ShopDomainManager

    fun provideGroupDomainManager(): GroupDomainManager

    fun provideUserDetailsDomainManager(): UserDetailsDomainManager

    fun provideCatalogDomainManager(): CatalogDomainManager

    fun provideSessionDomainManager(): SessionDomainManager

    fun provideConnectivityDomainManager(): ConnectivityDomainManager

    fun provideDatabaseDomainManager(): DatabaseDomainManager

    fun provideRushDomainManager(): RushDomainManager

    fun provideAccountActivitiesDomainManager(): AccountActivitiesDomainManager

    fun provideVoucherDomainManager(): VoucherDomainManager

    fun provideAppDataDomainManager(): AppDataDomainManager

    fun provideBillingsDomainManager(): BillingsDomainManager

    fun providePrepaidDomainManager(): PrepaidDomainManager

    fun provideBannersDomainManager(): BannersDomainManager

    fun provideMaintenanceDomainManager(): MaintenanceDomainManager

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance dataManagers: DataManagers,
            @BindsInstance repoManager: ReposManager
        ): UseCaseComponent
    }
}

@Module
interface DomainManagersModule {

    @Binds
    @UseCaseScope
    fun bindAuthDomainManager(authUseCaseManager: AuthUseCaseManager): AuthDomainManager

    @Binds
    @UseCaseScope
    fun bindProfileDomainManager(profileUseCaseManager: ProfileUseCaseManager): ProfileDomainManager

    @Binds
    @UseCaseScope
    fun bindBalanceDomainManager(balanceUseCaseManager: BalanceUseCaseManager): BalanceDomainManager

    @Binds
    @UseCaseScope
    fun bindAccountDomainManager(accountUseCaseManager: AccountUseCaseManager): AccountDomainManager

    @Binds
    @UseCaseScope
    fun bindPaymentDomainManager(paymentUseCaseManager: PaymentUseCaseManager): PaymentDomainManager

    @Binds
    @UseCaseScope
    fun bindRewardsDomainManager(rewardsUseCaseManager: RewardsUseCaseManager): RewardsDomainManager

    @Binds
    @UseCaseScope
    fun bindCreditDomainManager(creditUseCaseManager: CreditUserCaseManager): CreditDomainManager

    @Binds
    @UseCaseScope
    fun bindShopDomainManager(shopUseCaseManager: ShopUseCaseManager): ShopDomainManager

    @Binds
    @UseCaseScope
    fun bindGroupDomainManager(groupUseCaseManager: GroupUseCaseManager): GroupDomainManager

    @Binds
    @UseCaseScope
    fun bindUserDetailsDomainManager(userDetailsUseCaseManager: UserDetailsUseCaseManager): UserDetailsDomainManager

    @Binds
    @UseCaseScope
    fun bindCatalogDomainManager(catalogUseCaseManager: CatalogUseCaseManager): CatalogDomainManager

    @Binds
    @UseCaseScope
    fun bindSessionDomainManager(sessionUseCaseManager: SessionUseCaseManager): SessionDomainManager

    @Binds
    @UseCaseScope
    fun bindConnectivityDomainManager(connectivityUseCaseManager: ConnectivityUseCaseManager): ConnectivityDomainManager

    @Binds
    @UseCaseScope
    fun bindDatabaseDomainManager(databaseUseCaseManager: DatabaseUseCaseManager): DatabaseDomainManager

    @Binds
    @UseCaseScope
    fun bindRushDomainManager(rushUseCaseManager: RushUseCaseManager): RushDomainManager

    @Binds
    @UseCaseScope
    fun bindAccountActivitiesDomainManager(accountActivitiesUseCaseManager: AccountActivitiesUseCaseManager): AccountActivitiesDomainManager

    @Binds
    @UseCaseScope
    fun bindVoucherDomainManager(voucherUseCaseManager: VoucherUseCaseManager): VoucherDomainManager

    @Binds
    @UseCaseScope
    fun bindAppDataDomainManager(appDataUseCaseManager: AppDataUseCaseManager): AppDataDomainManager

    @Binds
    @UseCaseScope
    fun bindBillingsDomainManager(billingsUseCaseManager: BillingsUseCaseManager): BillingsDomainManager

    @Binds
    @UseCaseScope
    fun bindPrepaidDomainManager(prepaidUseCaseManager: PrepaidUseCaseManager): PrepaidDomainManager

    @Binds
    @UseCaseScope
    fun bindBannersDomainManager(bannersUseCaseManager: BannersUseCaseManager): BannersDomainManager

    @Binds
    @UseCaseScope
    fun bindMaintenanceDomainManager(maintenanceUseCaseManager: MaintenanceUseCaseManager): MaintenanceDomainManager
}

@Module
object DataManagersModule {

    @Provides
    fun provideAuthDataManager(dataManagers: DataManagers) = dataManagers.getAuthDataManager()

    @Provides
    fun provideProfileDataManager(dataManagers: DataManagers) = dataManagers.getProfileDataManager()

    @Provides
    fun provideBalanceDataManager(dataManagers: DataManagers) = dataManagers.getBalanceDataManager()

    @Provides
    fun provideAccountDataManager(dataManagers: DataManagers) = dataManagers.getAccountDataManager()

    @Provides
    fun providePaymentDataManager(dataManagers: DataManagers) = dataManagers.getPaymentDataManager()

    @Provides
    fun provideRewardsDataManager(dataManagers: DataManagers) = dataManagers.getRewardsDataManager()

    @Provides
    fun provideCreditDataManager(dataManagers: DataManagers) = dataManagers.getCreditDataManager()

    @Provides
    fun provideShopDataManager(dataManagers: DataManagers) = dataManagers.getShopDataManager()

    @Provides
    fun provideGroupDataManager(dataManagers: DataManagers) = dataManagers.getGroupDataManager()

    @Provides
    fun provideUserDetailsDataManager(dataManagers: DataManagers) =
        dataManagers.getUserDetailsDataManager()

    @Provides
    fun provideCatalogDataManager(dataManagers: DataManagers) = dataManagers.getCatalogDataManager()

    @Provides
    fun bindNetworkStatusProvider(dataManagers: DataManagers) =
        dataManagers.getConnectivityDataManager()

    @Provides
    fun provideRushDataManager(dataManagers: DataManagers) = dataManagers.getRushDataManager()

    @Provides
    fun bindAccountActivitiesDataManager(dataManagers: DataManagers) =
        dataManagers.getAccountActivitiesDataManager()

    @Provides
    fun bindVoucherDataManager(dataManagers: DataManagers) = dataManagers.getVoucherDataManager()

    @Provides
    fun bindBillingsDataManager(dataManagers: DataManagers) = dataManagers.getBillingsDataManager()

    @Provides
    fun bindPrepaidDataManager(dataManagers: DataManagers) = dataManagers.getPrepaidDataManager()

    @Provides
    fun bindBannersDataManager(dataManagers: DataManagers) = dataManagers.getBannersDataManager()

    @Provides
    fun bindMaintenanceDataManager(dataManagers: DataManagers) =
        dataManagers.getMaintenanceDataManager()
}
