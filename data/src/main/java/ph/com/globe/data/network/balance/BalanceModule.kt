/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.balance

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import ph.com.globe.data.ManagerScope
import ph.com.globe.data.network.GLOBE_SERVER
import ph.com.globe.data.network.balance.calls.CheckAmaxWalletBalanceSufficiencyNetworkCall
import ph.com.globe.data.network.balance.calls.CheckBalanceSufficiencyNetworkCall
import retrofit2.Retrofit
import javax.inject.Named

@Module(subcomponents = [BalanceComponent::class])
internal interface BalanceModule

@ManagerScope
@Subcomponent(modules = [BalanceProvidesModule::class])
interface BalanceComponent {

    fun provideCheckBalanceSufficiencyNetworkCall(): CheckBalanceSufficiencyNetworkCall

    fun provideCheckAmaxWalletBalanceSufficiencyNetworkCall(): CheckAmaxWalletBalanceSufficiencyNetworkCall

    @Subcomponent.Factory
    interface Factory {
        fun create(): BalanceComponent
    }
}

@Module
internal object BalanceProvidesModule {

    @Provides
    @ManagerScope
    fun provideBalanceRetrofit(@Named(GLOBE_SERVER) retrofit: Retrofit): BalanceRetrofit =
        retrofit.create(BalanceRetrofit::class.java)

}
