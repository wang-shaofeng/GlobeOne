/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data

import android.content.Context
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import ph.com.globe.data.db.di.DatabaseBindsModule
import ph.com.globe.data.db.di.DatabaseProvidesModule
import ph.com.globe.data.db.di.RepositoryManager
import ph.com.globe.data.network.NetworkManagers
import ph.com.globe.data.network.NetworkRetrofitModule
import ph.com.globe.data.network.account.AccountModule
import ph.com.globe.data.network.account.NetworkAccountManager
import ph.com.globe.data.network.account_activities.AccountActivitiesModule
import ph.com.globe.data.network.account_activities.NetworkAccountActivitiesManager
import ph.com.globe.data.network.auth.AuthModule
import ph.com.globe.data.network.auth.NetworkAuthManager
import ph.com.globe.data.network.balance.BalanceModule
import ph.com.globe.data.network.balance.NetworkBalanceManager
import ph.com.globe.data.network.banners.BannersModule
import ph.com.globe.data.network.banners.NetworkBannersManager
import ph.com.globe.data.network.billings.BillingsModule
import ph.com.globe.data.network.billings.NetworkBillingsManager
import ph.com.globe.data.network.catalog.CatalogModule
import ph.com.globe.data.network.catalog.NetworkCatalogManager
import ph.com.globe.data.network.credit.CreditModule
import ph.com.globe.data.network.credit.NetworkCreditManager
import ph.com.globe.data.network.group.GroupModule
import ph.com.globe.data.network.group.NetworkGroupManager
import ph.com.globe.data.network.maintenance.MaintenanceModule
import ph.com.globe.data.network.maintenance.NetworkMaintenanceManager
import ph.com.globe.data.network.payment.NetworkPaymentManager
import ph.com.globe.data.network.payment.PaymentModule
import ph.com.globe.data.network.prepaid.NetworkPrepaidManager
import ph.com.globe.data.network.prepaid.calls.PrepaidModule
import ph.com.globe.data.network.profile.NetworkProfileManager
import ph.com.globe.data.network.profile.ProfileModule
import ph.com.globe.data.network.rewards.NetworkRewardsManager
import ph.com.globe.data.network.rewards.RewardsModule
import ph.com.globe.data.network.rush.NetworkRushManager
import ph.com.globe.data.network.rush.RushModule
import ph.com.globe.data.network.shop.NetworkShopManager
import ph.com.globe.data.network.shop.ShopModule
import ph.com.globe.data.network.user_details.NetworkUserDetailsManager
import ph.com.globe.data.network.user_details.di.UserDetailsModule
import ph.com.globe.data.network.voucher.NetworkVoucherManager
import ph.com.globe.data.network.voucher.VoucherModule
import ph.com.globe.data.network_components.ConnectivityManager
import ph.com.globe.data.network_components.di.NetworkStatusModule
import ph.com.globe.data.shared_preferences.payment.di.PaymentParametersSharedPrefModule
import ph.com.globe.data.shared_preferences.session.di.UserSessionSharedPrefModule
import ph.com.globe.data.shared_preferences.token.di.TokenModule
import ph.com.globe.domain.DataManagers
import ph.com.globe.domain.ReposManager
import ph.com.globe.domain.account.AccountDataManager
import ph.com.globe.domain.account_activities.AccountActivitiesDataManager
import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.domain.balance.BalanceDataManager
import ph.com.globe.domain.banners.BannersDataManager
import ph.com.globe.domain.billings.BillingsDataManager
import ph.com.globe.domain.catalog.CatalogDataManager
import ph.com.globe.domain.connectivity.ConnectivityDataManager
import ph.com.globe.domain.credit.CreditDataManager
import ph.com.globe.domain.group.GroupDataManager
import ph.com.globe.domain.maintenance.MaintenanceDataManager
import ph.com.globe.domain.payment.PaymentDataManager
import ph.com.globe.domain.prepaid.PrepaidDataManager
import ph.com.globe.domain.profile.ProfileDataManager
import ph.com.globe.domain.rewards.RewardsDataManager
import ph.com.globe.domain.rush.RushDataManager
import ph.com.globe.domain.shop.ShopDataManager
import ph.com.globe.domain.user_details.UserDetailsDataManager
import ph.com.globe.domain.voucher.VoucherDataManager
import ph.com.globe.encryption.EncryptionComponent
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention(value = AnnotationRetention.RUNTIME)
internal annotation class DataScope

@Scope
@MustBeDocumented
@Retention(value = AnnotationRetention.RUNTIME)
internal annotation class ManagerScope

@DataScope
@Component(
    modules = [NetworkRetrofitModule::class, ManagersModule::class, TokenModule::class, UserSessionSharedPrefModule::class,
        PaymentParametersSharedPrefModule::class, AuthModule::class, ProfileModule::class, AccountModule::class, BalanceModule::class,
        PaymentModule::class, CatalogModule::class, RewardsModule::class, UserDetailsModule::class, CreditModule::class, ShopModule::class,
        GroupModule::class, DatabaseProvidesModule::class, DatabaseBindsModule::class, RepositoryBindsModule::class,
        NetworkStatusModule::class, RushModule::class, AccountActivitiesModule::class, VoucherModule::class, BillingsModule::class, PrepaidModule::class,
        BannersModule::class, MaintenanceModule::class
    ],
    dependencies = [EncryptionComponent::class]
)
interface DataComponent {

    fun provideDataManagers(): DataManagers

    fun provideReposManager(): ReposManager

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance context: Context,
            encryptionComponent: EncryptionComponent
        ): DataComponent
    }
}

@Module
interface ManagersModule {

    @Binds
    @DataScope
    fun bindDataManagers(dataManagers: NetworkManagers): DataManagers

    @Binds
    @DataScope
    fun bindAuthManager(networkAuthManager: NetworkAuthManager): AuthDataManager

    @Binds
    @DataScope
    fun bindProfileManager(networkAuthManager: NetworkProfileManager): ProfileDataManager

    @Binds
    @DataScope
    fun bindBalanceManager(networkBalanceManager: NetworkBalanceManager): BalanceDataManager

    @Binds
    @DataScope
    fun bindAccountManager(networkAccountManager: NetworkAccountManager): AccountDataManager

    @Binds
    @DataScope
    fun bindPaymentManager(networkPaymentManager: NetworkPaymentManager): PaymentDataManager

    @Binds
    @DataScope
    fun bindRewardsManager(networkRewardsManager: NetworkRewardsManager): RewardsDataManager

    @Binds
    @DataScope
    fun bindCreditManager(networkCreditManger: NetworkCreditManager): CreditDataManager

    @Binds
    @DataScope
    fun bindShopManager(networkShopManager: NetworkShopManager): ShopDataManager

    @Binds
    @DataScope
    fun bindGroupManager(networkGroupManager: NetworkGroupManager): GroupDataManager

    @Binds
    @DataScope
    fun bindUserDetailsManager(networkUserDetailsManager: NetworkUserDetailsManager): UserDetailsDataManager

    @Binds
    @DataScope
    fun bindCatalogManager(networkCatalogManager: NetworkCatalogManager): CatalogDataManager

    @Binds
    @DataScope
    fun bindConnectivityManager(connectivityDataManager: ConnectivityManager): ConnectivityDataManager

    @Binds
    @DataScope
    fun bindRushManager(networkRushDataManager: NetworkRushManager): RushDataManager

    @Binds
    @DataScope
    fun bindAccountActivities(accountActivitiesManager: NetworkAccountActivitiesManager): AccountActivitiesDataManager

    @Binds
    @DataScope
    fun bindVoucherManager(networkVoucherManager: NetworkVoucherManager): VoucherDataManager

    @Binds
    @DataScope
    fun bindBillingsManager(networkBillingsManager: NetworkBillingsManager): BillingsDataManager

    @Binds
    @DataScope
    fun bindPrepaidDataManager(networkPrepaidManager: NetworkPrepaidManager): PrepaidDataManager

    @Binds
    @DataScope
    fun bindBannersDataManager(networkBannersManager: NetworkBannersManager): BannersDataManager

    @Binds
    @DataScope
    fun bindMaintenanceDataManager(networkMaintenanceManager: NetworkMaintenanceManager): MaintenanceDataManager
}

@Module
interface RepositoryBindsModule {

    @Binds
    @DataScope
    fun bindRepoManagers(repoManagers: RepositoryManager): ReposManager
}
