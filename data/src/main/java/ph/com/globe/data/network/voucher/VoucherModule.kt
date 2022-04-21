/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.voucher

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import ph.com.globe.data.ManagerScope
import ph.com.globe.data.network.GLOBE_SERVER
import ph.com.globe.data.network.voucher.calls.GetLoyaltySubscribersCouponDetailsNetworkCall
import ph.com.globe.data.network.voucher.calls.GetPromoVouchersNetworkCall
import ph.com.globe.data.network.voucher.calls.MarkVoucherAsUsedNetworkCall
import ph.com.globe.data.network.voucher.calls.RetrieveUsedVouchersNetworkCall
import retrofit2.Retrofit
import javax.inject.Named

@Module(subcomponents = [VoucherComponent::class])
internal interface VoucherModule

@ManagerScope
@Subcomponent(modules = [VoucherProvidesModule::class])
interface VoucherComponent {

    fun provideGetLoyaltySubscribersCouponDetailsNetworkCall(): GetLoyaltySubscribersCouponDetailsNetworkCall

    fun provideMarkVoucherAsUsedNetworkCall(): MarkVoucherAsUsedNetworkCall

    fun provideRetrieveUsedVouchersNetworkCall(): RetrieveUsedVouchersNetworkCall

    fun provideGetPromoVouchersNetworkCall(): GetPromoVouchersNetworkCall

    @Subcomponent.Factory
    interface Factory {
        fun create(): VoucherComponent
    }
}

@Module
internal object VoucherProvidesModule {

    @Provides
    @ManagerScope
    fun provideVoucherRetrofit(@Named(GLOBE_SERVER) retrofit: Retrofit): VoucherRetrofit =
        retrofit.create(VoucherRetrofit::class.java)

}
