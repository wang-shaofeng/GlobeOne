/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.banners

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import ph.com.globe.data.ManagerScope
import ph.com.globe.data.network.CMS_SERVER
import ph.com.globe.data.network.banners.repositories.BannersRepository
import ph.com.globe.data.network.banners.repositories.BannersRepositoryModule
import retrofit2.Retrofit
import javax.inject.Named

@Module(subcomponents = [BannersComponent::class])
internal interface BannersModule

@ManagerScope
@Subcomponent(modules = [BannersProvidesModule::class, BannersRepositoryModule::class])
interface BannersComponent {

    fun provideFetchBannersNetworkCall(): FetchBannersNetworkCall

    fun provideBannersRepository(): BannersRepository

    @Subcomponent.Factory
    interface Factory {
        fun create(): BannersComponent
    }
}

@Module
internal object BannersProvidesModule {

    @Provides
    @ManagerScope
    fun providesBannersRetrofit(@Named(CMS_SERVER) retrofit: Retrofit): BannersRetrofit =
        retrofit.create(BannersRetrofit::class.java)
}
