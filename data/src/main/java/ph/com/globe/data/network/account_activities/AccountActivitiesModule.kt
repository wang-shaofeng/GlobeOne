/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.account_activities

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import ph.com.globe.data.ManagerScope
import ph.com.globe.data.network.GLOBE_SERVER
import ph.com.globe.data.network.account_activities.calls.GetRewardsHistoryNetworkCall
import retrofit2.Retrofit
import javax.inject.Named

@Module(subcomponents = [AccountActivitiesComponent::class])
internal interface AccountActivitiesModule

@ManagerScope
@Subcomponent(modules = [AccountActivitiesProvidesModule::class])
interface AccountActivitiesComponent {

    fun provideGetRewardsHistoryNetworkCall(): GetRewardsHistoryNetworkCall

    @Subcomponent.Factory
    interface Factory {
        fun create(): AccountActivitiesComponent
    }
}

@Module
internal object AccountActivitiesProvidesModule {

    @Provides
    @ManagerScope
    fun providesAccountActivitiesRetrofit(@Named(GLOBE_SERVER) retrofit: Retrofit): AccountActivitiesRetrofit =
        retrofit.create(AccountActivitiesRetrofit::class.java)
}
