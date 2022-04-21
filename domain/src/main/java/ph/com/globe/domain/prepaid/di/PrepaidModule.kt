package ph.com.globe.domain.prepaid.di

import dagger.Module
import dagger.Subcomponent
import ph.com.globe.domain.ManagerScope
import ph.com.globe.domain.prepaid.GetAccountManagementTransactionUseCase

@Module(subcomponents = [PrepaidComponent::class])
internal interface PrepaidModule

@ManagerScope
@Subcomponent
interface PrepaidComponent {

    fun provideGetAccountManagementTransactionUseCase() : GetAccountManagementTransactionUseCase

    @Subcomponent.Factory
    interface Factory {
        fun create(): PrepaidComponent
    }
}
