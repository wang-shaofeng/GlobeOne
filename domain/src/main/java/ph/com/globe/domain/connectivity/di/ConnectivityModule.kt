/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.connectivity.di

import dagger.Module
import dagger.Subcomponent
import ph.com.globe.domain.ManagerScope
import ph.com.globe.domain.connectivity.usecases.ConnectivityUseCase

@Module(subcomponents = [ConnectivityComponent::class])
internal interface ConnectivityModule

@ManagerScope
@Subcomponent
interface ConnectivityComponent {
    fun provideConnectivityUseCase(): ConnectivityUseCase

    @Subcomponent.Factory
    interface Factory {
        fun create(): ConnectivityComponent
    }
}
