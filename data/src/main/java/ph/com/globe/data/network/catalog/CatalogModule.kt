/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.catalog

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import ph.com.globe.data.ManagerScope
import ph.com.globe.data.network.GLOBE_SERVER
import ph.com.globe.data.network.catalog.calls.*
import retrofit2.Retrofit
import javax.inject.Named

@Module(subcomponents = [CatalogComponent::class])
internal interface CatalogModule

@ManagerScope
@Subcomponent(modules = [CatalogProvidesModule::class])
interface CatalogComponent {

    fun provideContentSubscriptionStatusNetworkCall(): ContentSubscriptionStatusNetworkCall

    fun provideProvisionContentPromoNetworkCall(): ProvisionContentPromoNetworkCall

    fun provideUnsubscribeContentPromoNetworkCall(): UnsubscribeContentPromoNetworkCall

    @Subcomponent.Factory
    interface Factory {
        fun create(): CatalogComponent
    }
}

@Module
internal object CatalogProvidesModule {

    @Provides
    @ManagerScope
    fun providesCatalogRetrofit(@Named(GLOBE_SERVER) retrofit: Retrofit): CatalogRetrofit =
        retrofit.create(CatalogRetrofit::class.java)
}
