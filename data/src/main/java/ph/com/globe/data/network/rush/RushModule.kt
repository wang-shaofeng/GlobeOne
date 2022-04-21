/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.rush

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import ph.com.globe.data.ManagerScope
import ph.com.globe.data.network.RUSH_SERVER
import ph.com.globe.data.network.rush.calls.CreateRushUserNetworkCall
import ph.com.globe.data.network.rush.calls.GetRushAdminAccessTokenNetworkCall
import ph.com.globe.data.network.rush.calls.GetRushUserAccessTokenNetworkCall
import retrofit2.Retrofit
import javax.inject.Named

@Module(subcomponents = [RushComponent::class])
internal interface RushModule

@ManagerScope
@Subcomponent(modules = [RushProvidesModule::class])
interface RushComponent {
    fun provideGetRushUserAccessTokenNetworkCall(): GetRushUserAccessTokenNetworkCall

    fun provideGetRushAdminAccessTokenNetworkCall(): GetRushAdminAccessTokenNetworkCall

    fun provideCreateRushUserNetworkCall(): CreateRushUserNetworkCall

    @Subcomponent.Factory
    interface Factory {
        fun create(): RushComponent
    }
}

@Module
internal object RushProvidesModule {

    @Provides
    @ManagerScope
    fun providesRushRetrofit(@Named(RUSH_SERVER) retrofit: Retrofit): RushRetrofit =
        retrofit.create(RushRetrofit::class.java)

    @Provides
    @ManagerScope
    fun providesRushTokenManager(): RushTokenManager = RushTokenManager()
}
