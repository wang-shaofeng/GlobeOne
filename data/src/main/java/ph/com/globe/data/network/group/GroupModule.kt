/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.group

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import ph.com.globe.data.ManagerScope
import ph.com.globe.data.network.GLOBE_SERVER
import ph.com.globe.data.network.group.calls.*
import retrofit2.Retrofit
import javax.inject.Named

@Module(subcomponents = [GroupComponent::class])
internal interface GroupModule

@ManagerScope
@Subcomponent(modules = [GroupProvidesModule::class])
interface GroupComponent {

    fun provideGetGroupListNetworkCall(): GetGroupListNetworkCall

    fun provideAddGroupMemberNetworkCall(): AddGroupMemberNetworkCall

    fun provideRetrieveGroupUsageNetworkCall(): RetrieveGroupUsageNetworkCall

    fun provideRetrieveMemberUsageNetworkCall(): RetrieveMemberUsageNetworkCall

    fun provideRetrieveGroupServiceNetworkCall(): RetrieveGroupServiceNetworkCall

    fun provideDeleteGroupMemberNetworkCall(): DeleteGroupMemberNetworkCall

    fun provideSetMemberUsageLimitNetworkCall(): SetMemberUsageLimitNetworkCall

    @Subcomponent.Factory
    interface Factory {
        fun create(): GroupComponent
    }
}

@Module
internal object GroupProvidesModule {

    @Provides
    @ManagerScope
    fun provideGroupRetrofit(@Named(GLOBE_SERVER) retrofit: Retrofit): GroupRetrofit =
        retrofit.create(GroupRetrofit::class.java)
}
