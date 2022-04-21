/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.rewards

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import ph.com.globe.data.ManagerScope
import ph.com.globe.data.network.GLOBE_SERVER
import ph.com.globe.data.network.rewards.calls.*
import ph.com.globe.data.network.rewards.repositories.RewardsRepository
import ph.com.globe.data.network.rewards.repositories.RewardsRepositoryModule
import retrofit2.Retrofit
import javax.inject.Named

@Module(subcomponents = [RewardsComponent::class])
internal interface RewardsModule

@ManagerScope
@Subcomponent(modules = [RewardsProvidesModule::class, RewardsRepositoryModule::class])
interface RewardsComponent {

    fun provideGetRewardPointsNetworkCall(): GetRewardPointsNetworkCall

    fun provideGetConversionQualificationNetworkCall(): GetConversionQualificationNetworkCall

    fun provideAddDataConversionNetworkCall(): AddDataConversionNetworkCall

    fun provideGetDataConversionDetailsNetworkCall(): GetDataConversionDetailsNetworkCall

    fun provideRewardsCatalogNetworkCall(): RewardsCatalogNetworkCall

    fun provideRewardsRepository(): RewardsRepository

    fun provideGetLoyaltyCustomerProfileCall(): GetLoyaltyCustomerProfileCall

    fun provideRedeemLoyaltyRewardsNetworkCall(): RedeemLoyaltyRewardsNetworkCall

    fun provideGetMerchantDetailsNetworkCall(): GetMerchantDetailsNetworkCall

    fun provideRedeemPointsNetworkCall(): RedeemPointsNetworkCall

    @Subcomponent.Factory
    interface Factory {
        fun create(): RewardsComponent
    }
}

@Module
internal object RewardsProvidesModule {

    @Provides
    @ManagerScope
    fun providesRewardsRetrofit(@Named(GLOBE_SERVER) retrofit: Retrofit): RewardsRetrofit =
        retrofit.create(RewardsRetrofit::class.java)
}
