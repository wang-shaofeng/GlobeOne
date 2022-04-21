/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.maintenance.di

import dagger.Module
import dagger.Subcomponent
import ph.com.globe.domain.ManagerScope
import ph.com.globe.domain.maintenance.usecase.MaintenanceUseCase

@Module(subcomponents = [MaintenanceComponent::class])
internal interface MaintenanceModule

@ManagerScope
@Subcomponent
interface MaintenanceComponent {

    fun provideGetMaintenanceUseCase(): MaintenanceUseCase

    @Subcomponent.Factory
    interface Factory {
        fun create(): MaintenanceComponent
    }
}
