/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.balance.di

import dagger.Module
import dagger.Subcomponent
import ph.com.globe.domain.ManagerScope
import ph.com.globe.domain.balance.usecase.CheckAmaxWalletBalanceSufficiencyUseCase
import ph.com.globe.domain.balance.usecase.CheckBalanceSufficiencyUseCase

@Module(subcomponents = [BalanceComponent::class])
internal interface BalanceModule

@ManagerScope
@Subcomponent
interface BalanceComponent {

    fun provideCheckBalanceSufficiencyUseCase(): CheckBalanceSufficiencyUseCase

    fun provideCheckAmaxWalletBalanceSufficiencyUseCase(): CheckAmaxWalletBalanceSufficiencyUseCase

    @Subcomponent.Factory
    interface Factory {
        fun create(): BalanceComponent
    }
}
