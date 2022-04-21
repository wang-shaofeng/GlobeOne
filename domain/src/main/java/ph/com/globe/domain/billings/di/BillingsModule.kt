package ph.com.globe.domain.billings.di

import dagger.Module
import dagger.Subcomponent
import ph.com.globe.domain.ManagerScope
import ph.com.globe.domain.billings.usecase.GetBillingsDetailsUseCase
import ph.com.globe.domain.billings.usecase.GetBillingsStatementsPdfUseCase
import ph.com.globe.domain.billings.usecase.GetBillingsStatementsUseCase

@Module(subcomponents = [BillingsComponent::class])
internal interface BillingsModule

@ManagerScope
@Subcomponent
interface BillingsComponent {

    fun provideGetBillingsDetailsUseCase(): GetBillingsDetailsUseCase

    fun provideGetBillingsStatementsUseCase(): GetBillingsStatementsUseCase

    fun provideGetBillingsStatementsPdfUseCase(): GetBillingsStatementsPdfUseCase

    @Subcomponent.Factory
    interface Factory {
        fun create(): BillingsComponent
    }
}
