/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.maintenance

import ph.com.globe.errors.maintenance.GetMaintenanceError
import ph.com.globe.model.maintenance.MaintenanceModel
import ph.com.globe.util.LfResult

interface MaintenanceDomainManager {

    suspend fun getLoginMaintenance(): LfResult<MaintenanceModel, GetMaintenanceError>

    suspend fun getDashboardMaintenance(): LfResult<MaintenanceModel, GetMaintenanceError>

    suspend fun getRewardsMaintenance(): LfResult<MaintenanceModel, GetMaintenanceError>

    suspend fun getShopMaintenance(): LfResult<MaintenanceModel, GetMaintenanceError>

    suspend fun getAccountMaintenance(): LfResult<MaintenanceModel, GetMaintenanceError>

    suspend fun getDiscoverMaintenance(): LfResult<MaintenanceModel, GetMaintenanceError>
}
