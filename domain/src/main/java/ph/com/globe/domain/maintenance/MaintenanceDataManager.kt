/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.maintenance

import ph.com.globe.errors.maintenance.GetMaintenanceError
import ph.com.globe.model.maintenance.MaintenanceData
import ph.com.globe.util.LfResult

interface MaintenanceDataManager {

    suspend fun getLoginMaintenance(): LfResult<MaintenanceData, GetMaintenanceError>

    suspend fun getDashboardMaintenance(): LfResult<MaintenanceData, GetMaintenanceError>

    suspend fun getRewardsMaintenance(): LfResult<MaintenanceData, GetMaintenanceError>

    suspend fun getShopMaintenance(): LfResult<MaintenanceData, GetMaintenanceError>

    suspend fun getAccountMaintenance(): LfResult<MaintenanceData, GetMaintenanceError>

    suspend fun getDiscoverMaintenance(): LfResult<MaintenanceData, GetMaintenanceError>
}
