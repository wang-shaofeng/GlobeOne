package ph.com.globe.data.network.prepaid.calls

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import ph.com.globe.data.ManagerScope
import ph.com.globe.data.network.GLOBE_SERVER
import ph.com.globe.data.network.prepaid.PrepaidRetrofit
import retrofit2.Retrofit
import javax.inject.Named


@Module(subcomponents = [PrepaidComponent::class])
internal interface PrepaidModule

@ManagerScope
@Subcomponent(modules = [PrepaidProvidesModule::class])
interface PrepaidComponent {

    fun provideGetAccountManagementTransactionNetworkCall(): GetAccountManagementTransactionNetworkCall

    @Subcomponent.Factory
    interface Factory {
        fun create(): PrepaidComponent
    }
}

@Module
internal object PrepaidProvidesModule {

    @Provides
    @ManagerScope
    fun providesPrepaidRetrofit(@Named(GLOBE_SERVER) retrofit: Retrofit): PrepaidRetrofit =
        retrofit.create(PrepaidRetrofit::class.java)
}
