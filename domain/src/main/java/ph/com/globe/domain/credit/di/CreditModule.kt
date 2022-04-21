/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.credit.di

import dagger.Module
import dagger.Subcomponent
import ph.com.globe.domain.ManagerScope
import ph.com.globe.domain.credit.usecase.GetCreditInfoUseCase
import ph.com.globe.domain.credit.usecase.LoanPromoUseCase

@Module(subcomponents = [CreditComponent::class])
internal interface CreditModule

@ManagerScope
@Subcomponent
interface CreditComponent {

    fun provideGetCreditInfoUseCase(): GetCreditInfoUseCase

    fun provideLoanPromoUseCase(): LoanPromoUseCase

    @Subcomponent.Factory
    interface Factory {
        fun create(): CreditComponent
    }
}
