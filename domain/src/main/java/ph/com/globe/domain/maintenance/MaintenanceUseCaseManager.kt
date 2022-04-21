/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.maintenance

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ph.com.globe.domain.maintenance.di.MaintenanceComponent
import ph.com.globe.errors.maintenance.GetMaintenanceError
import ph.com.globe.model.maintenance.MaintenanceModel
import ph.com.globe.util.LfResult
import javax.inject.Inject

class MaintenanceUseCaseManager @Inject constructor(
    factory: MaintenanceComponent.Factory
) : MaintenanceDomainManager {

    private val maintenanceComponent: MaintenanceComponent = factory.create()

    override suspend fun getLoginMaintenance(): LfResult<MaintenanceModel, GetMaintenanceError> =
        withContext(Dispatchers.IO) {
            maintenanceComponent.provideGetMaintenanceUseCase().getLoginMaintenance()
        }

    override suspend fun getDashboardMaintenance(): LfResult<MaintenanceModel, GetMaintenanceError> =
        withContext(Dispatchers.IO) {
            maintenanceComponent.provideGetMaintenanceUseCase().getDashboardMaintenance()
        }

    override suspend fun getRewardsMaintenance(): LfResult<MaintenanceModel, GetMaintenanceError> =
        withContext(Dispatchers.IO) {
            maintenanceComponent.provideGetMaintenanceUseCase().getRewardsMaintenance()
        }

    override suspend fun getShopMaintenance(): LfResult<MaintenanceModel, GetMaintenanceError> =
        withContext(Dispatchers.IO) {
            maintenanceComponent.provideGetMaintenanceUseCase().getShopMaintenance()
        }

    override suspend fun getAccountMaintenance(): LfResult<MaintenanceModel, GetMaintenanceError> =
        withContext(Dispatchers.IO) {
            maintenanceComponent.provideGetMaintenanceUseCase().getAccountMaintenance()
        }

    override suspend fun getDiscoverMaintenance(): LfResult<MaintenanceModel, GetMaintenanceError> =
        withContext(Dispatchers.IO) {
            maintenanceComponent.provideGetMaintenanceUseCase().getDiscoverMaintenance()
        }
}
