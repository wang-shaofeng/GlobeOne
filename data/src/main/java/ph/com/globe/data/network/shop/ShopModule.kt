/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.shop

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import ph.com.globe.data.ManagerScope
import ph.com.globe.data.network.GLOBE_SERVER
import ph.com.globe.data.network.LF_SERVER
import ph.com.globe.data.network.OCS_SERVER
import ph.com.globe.data.network.shop.calls.GetAllOffersNetworkCall
import ph.com.globe.data.network.shop.calls.GetPromoSubscriptionHistoryNetworkCall
import ph.com.globe.data.network.shop.calls.ValidateRetailerNetworkCall
import retrofit2.Retrofit
import javax.inject.Named

@Module(subcomponents = [ShopComponent::class])
internal interface ShopModule

@ManagerScope
@Subcomponent(modules = [ShopProvidesModule::class])
interface ShopComponent {

    fun provideGetAllOffersNetworkCall(): GetAllOffersNetworkCall

    fun provideValidateRetailerNetworkCall(): ValidateRetailerNetworkCall

    fun provideGetPromoSubscriptionHistoryNetworkCall(): GetPromoSubscriptionHistoryNetworkCall

    @Subcomponent.Factory
    interface Factory {
        fun create(): ShopComponent
    }
}

@Module
internal object ShopProvidesModule {

    @Provides
    @ManagerScope
    fun providesShopLFRetrofit(@Named(LF_SERVER) retrofit: Retrofit): ShopLFRetrofit =
        retrofit.create(ShopLFRetrofit::class.java)

    @Provides
    @ManagerScope
    fun providesOCSShopRetrofit(@Named(OCS_SERVER) retrofit: Retrofit): OCSShopRetrofit =
        retrofit.create(OCSShopRetrofit::class.java)

    @Provides
    @ManagerScope
    fun providesShopGlobeRetrofit(@Named(GLOBE_SERVER) retrofit: Retrofit): ShopGlobeRetrofit =
        retrofit.create(ShopGlobeRetrofit::class.java)
}
