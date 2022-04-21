/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.billings

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import ph.com.globe.data.ManagerScope
import ph.com.globe.data.network.GLOBE_SERVER
import ph.com.globe.data.network.billings.calls.GetBillingsDetailsNetworkCall
import ph.com.globe.data.network.billings.calls.GetBillingsStatementsNetworkCall
import ph.com.globe.data.network.billings.calls.GetBillingsStatementsPdfNetworkCall
import retrofit2.Retrofit
import javax.inject.Named

@Module(subcomponents = [BillingsComponent::class])
internal interface BillingsModule

@ManagerScope
@Subcomponent(modules = [BillingsProvidesModule::class])
interface BillingsComponent {

    fun provideGetBillingsDetailsNetworkCall(): GetBillingsDetailsNetworkCall

    fun provideGetBillingsStatementsNetworkCall(): GetBillingsStatementsNetworkCall

    fun provideGetBillingsStatementsPdfNetworkCall(): GetBillingsStatementsPdfNetworkCall

    @Subcomponent.Factory
    interface Factory {
        fun create(): BillingsComponent
    }
}

@Module
internal object BillingsProvidesModule {

    @Provides
    @ManagerScope
    fun provideBillingsRetrofit(@Named(GLOBE_SERVER) retrofit: Retrofit): BillingsRetrofit =
        retrofit.create(BillingsRetrofit::class.java)

}
