/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.maintenance.repositories

import dagger.Binds
import dagger.Module
import ph.com.globe.data.ManagerScope

@Module
internal interface MaintenanceRepositoryModule {

    @Binds
    @ManagerScope
    fun bindMaintenanceRepository(maintenanceRepository: DefaultMaintenanceRepository): MaintenanceRepository
}
