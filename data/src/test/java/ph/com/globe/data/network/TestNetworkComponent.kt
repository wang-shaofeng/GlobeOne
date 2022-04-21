/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network

import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ph.com.globe.data.DataComponent
import ph.com.globe.data.DataScope
import ph.com.globe.data.ManagersModule
import ph.com.globe.data.network.account.AccountModule
import ph.com.globe.data.network.account_activities.AccountActivitiesModule
import ph.com.globe.data.network.auth.AuthModule
import ph.com.globe.data.network.balance.BalanceModule
import ph.com.globe.data.network.banners.BannersModule
import ph.com.globe.data.network.billings.BillingsModule
import ph.com.globe.data.network.catalog.CatalogModule
import ph.com.globe.data.network.credit.CreditModule
import ph.com.globe.data.network.group.GroupModule
import ph.com.globe.data.network.maintenance.MaintenanceModule
import ph.com.globe.data.network.payment.PaymentModule
import ph.com.globe.data.network.prepaid.calls.PrepaidModule
import ph.com.globe.data.network.profile.ProfileModule
import ph.com.globe.data.network.rewards.RewardsModule
import ph.com.globe.data.network.rush.RushModule
import ph.com.globe.data.network.shop.ShopModule
import ph.com.globe.data.network.user_details.di.TestUserDetailsModule
import ph.com.globe.data.network.voucher.VoucherModule
import ph.com.globe.data.network_components.TestNetworkStatusModule
import ph.com.globe.data.shared_preferences.token.di.TestTokenModule
import ph.com.globe.domain.ReposManager

@DataScope
@Component(
    modules = [TestNetworkRetrofitModule::class, ManagersModule::class, TestTokenModule::class, AuthModule::class,
        ProfileModule::class, AccountModule::class, BalanceModule::class, PaymentModule::class, RewardsModule::class, CatalogModule::class,
        TestUserDetailsModule::class, CreditModule::class, ShopModule::class, GroupModule::class, TestRepositoryBindsModule::class,
        TestNetworkStatusModule::class, RushModule::class, AccountActivitiesModule::class, VoucherModule::class,
        BillingsModule::class, PrepaidModule::class, BannersModule::class, MaintenanceModule::class
    ]
)
internal interface TestDataComponent : DataComponent {

    @Component.Factory
    interface Factory {
        fun create(): TestDataComponent
    }
}

@Module
@InstallIn(SingletonComponent::class)
interface TestRepositoryBindsModule {

    @Binds
    @DataScope
    fun bindRepoManagers(repoManagers: TestRepoManager): ReposManager
}
