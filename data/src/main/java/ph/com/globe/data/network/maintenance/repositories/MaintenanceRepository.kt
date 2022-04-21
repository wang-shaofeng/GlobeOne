/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.maintenance.repositories

import ph.com.globe.errors.maintenance.GetMaintenanceError
import ph.com.globe.model.maintenance.MaintenanceData
import ph.com.globe.util.LfResult

interface MaintenanceRepository {

    suspend fun getLoginMaintenance(): LfResult<MaintenanceData, GetMaintenanceError>

    suspend fun getDashboardMaintenance(): LfResult<MaintenanceData, GetMaintenanceError>

    suspend fun getRewardsMaintenance(): LfResult<MaintenanceData, GetMaintenanceError>

    suspend fun getShopMaintenance(): LfResult<MaintenanceData, GetMaintenanceError>

    suspend fun getAccountMaintenance(): LfResult<MaintenanceData, GetMaintenanceError>

    suspend fun getDiscoverMaintenance(): LfResult<MaintenanceData, GetMaintenanceError>

    suspend fun saveLoginMaintenance(maintenanceData: MaintenanceData)

    suspend fun saveDashboardMaintenance(maintenanceData: MaintenanceData)

    suspend fun saveRewardsMaintenance(maintenanceData: MaintenanceData)

    suspend fun saveShopMaintenance(maintenanceData: MaintenanceData)

    suspend fun saveAccountMaintenance(maintenanceData: MaintenanceData)

    suspend fun saveDiscoverMaintenance(maintenanceData: MaintenanceData)
}
