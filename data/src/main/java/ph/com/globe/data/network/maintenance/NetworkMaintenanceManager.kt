/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.maintenance

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ph.com.globe.domain.maintenance.MaintenanceDataManager
import ph.com.globe.errors.maintenance.GetMaintenanceError
import ph.com.globe.model.maintenance.MaintenanceData
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

/**
 * data strategy: remote only
 * if cache has data, return cache data else return remote data.
 */
class NetworkMaintenanceManager @Inject constructor(factory: MaintenanceComponent.Factory) :
    MaintenanceDataManager {

    private val maintenanceComponent: MaintenanceComponent = factory.create()

    override suspend fun getLoginMaintenance(): LfResult<MaintenanceData, GetMaintenanceError> =
        withContext(Dispatchers.IO) {
            maintenanceComponent.provideGetMaintenanceNetworkCall().getLoginMaintenance().also {
                it.fold({
                    maintenanceComponent.provideMaintenanceRepository().saveLoginMaintenance(it)
                }, {})
            }
        }

    override suspend fun getDashboardMaintenance(): LfResult<MaintenanceData, GetMaintenanceError> =
        withContext(Dispatchers.IO) {
            maintenanceComponent.provideGetMaintenanceNetworkCall().getDashboardMaintenance()
                .also {
                    it.fold({
                        maintenanceComponent.provideMaintenanceRepository()
                            .saveDashboardMaintenance(it)
                    }, {})
                }
        }

    override suspend fun getRewardsMaintenance(): LfResult<MaintenanceData, GetMaintenanceError> =
        withContext(Dispatchers.IO) {
            maintenanceComponent.provideGetMaintenanceNetworkCall().getRewardsMaintenance()
                .also {
                    it.fold({
                        maintenanceComponent.provideMaintenanceRepository()
                            .saveRewardsMaintenance(it)
                    }, {})
                }
        }

    override suspend fun getShopMaintenance(): LfResult<MaintenanceData, GetMaintenanceError> =
        withContext(Dispatchers.IO) {
            maintenanceComponent.provideGetMaintenanceNetworkCall().getShopMaintenance().also {
                it.fold({
                    maintenanceComponent.provideMaintenanceRepository().saveShopMaintenance(it)
                }, {})
            }
        }

    override suspend fun getAccountMaintenance(): LfResult<MaintenanceData, GetMaintenanceError> =
        withContext(Dispatchers.IO) {
            maintenanceComponent.provideGetMaintenanceNetworkCall().getAccountMaintenance()
                .also {
                    it.fold({
                        maintenanceComponent.provideMaintenanceRepository()
                            .saveAccountMaintenance(it)
                    }, {})
                }
        }

    override suspend fun getDiscoverMaintenance(): LfResult<MaintenanceData, GetMaintenanceError> =
        withContext(Dispatchers.IO) {
            maintenanceComponent.provideGetMaintenanceNetworkCall().getDiscoverMaintenance()
                .also {
                    it.fold({
                        maintenanceComponent.provideMaintenanceRepository()
                            .saveDiscoverMaintenance(it)
                    }, {})
                }
        }
}
