/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.payment

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import ph.com.globe.data.ManagerScope
import ph.com.globe.data.network.GLOBE_SERVER
import ph.com.globe.data.network.payment.calls.*
import retrofit2.Retrofit
import javax.inject.Named

@Module(subcomponents = [PaymentComponent::class])
internal interface PaymentModule

@ManagerScope
@Subcomponent(modules = [PaymentProvidesModule::class])
interface PaymentComponent {

    fun provideCreateAdyenPaymentSessionNetworkCall(): CreateAdyenPaymentSessionNetworkCall

    fun provideCreateGCashPaymentSessionNetworkCall(): CreateGCashPaymentSessionNetworkCall

    fun provideGetPaymentSessionNetworkCall(): GetPaymentSessionNetworkCall

    fun providePaymentServiceNetworkCall(): PaymentServiceNetworkCall

    fun provideGetPaymentMethodNetworkCall(): GetPaymentMethodNetworkCall

    fun provideDeletePaymentMethodNetworkCall(): DeletePaymentMethodNetworkCall

    fun provideLinkGCashAccountNetworkCall(): LinkGCashAccountNetworkCall

    fun provideUnlinkGCashAccountNetworkCall(): UnlinkGCashAccountNetworkCall

    fun providePurchasePromoNetworkCall(): PurchasePromoNetworkCall

    fun provideMultiplePurchasePromoNetworkCall(): MultiplePurchasePromoNetworkCall

    fun providePurchaseLoadNetworkCall(): PurchaseLoadNetworkCall

    fun provideCreateServiceOrderLoadNetworkCall(): CreateServiceOrderLoadNetworkCall

    fun provideCreateServiceOrderPromoNetworkCall(): CreateServiceOrderPromoNetworkCall

    fun provideGetGCashBalanceNetworkCall(): GetGCashBalanceNetworkCall

    fun provideGetPaymentsNetworkCall(): GetPaymentsNetworkCall

    fun provideGetPaymentReceiptNetworkCall(): GetPaymentReceiptNetworkCall

    @Subcomponent.Factory
    interface Factory {
        fun create(): PaymentComponent
    }
}

@Module
internal object PaymentProvidesModule {

    @Provides
    @ManagerScope
    fun providesPaymentRetrofit(@Named(GLOBE_SERVER) retrofit: Retrofit): PaymentRetrofit =
        retrofit.create(PaymentRetrofit::class.java)
}
