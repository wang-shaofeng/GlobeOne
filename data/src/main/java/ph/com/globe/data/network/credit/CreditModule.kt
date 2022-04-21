/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.credit

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import ph.com.globe.data.ManagerScope
import ph.com.globe.data.network.GLOBE_SERVER
import ph.com.globe.data.network.credit.calls.GetCreditInfoNetworkCall
import ph.com.globe.data.network.credit.calls.LoanPromoNetworkCall
import retrofit2.Retrofit
import javax.inject.Named

@Module(subcomponents = [CreditComponent::class])
internal interface CreditModule

@ManagerScope
@Subcomponent(modules = [CreditProvidesModule::class])
interface CreditComponent {

    fun provideGetCreditInfoNetworkCall(): GetCreditInfoNetworkCall

    fun provideLoanPromoNetworkCall(): LoanPromoNetworkCall

    @Subcomponent.Factory
    interface Factory {
        fun create(): CreditComponent
    }
}

@Module
internal object CreditProvidesModule {

    @Provides
    @ManagerScope
    fun provideCreditRetrofit(@Named(GLOBE_SERVER) retrofit: Retrofit): CreditRetrofit =
        retrofit.create(CreditRetrofit::class.java)
}
