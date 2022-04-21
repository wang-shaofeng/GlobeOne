/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.maintenance.repositories

import ph.com.globe.data.network.maintenance.calls.LandingType
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.maintenance.GetMaintenanceError
import ph.com.globe.model.maintenance.MaintenanceData
import ph.com.globe.util.LfResult
import javax.inject.Inject

class DefaultMaintenanceRepository @Inject constructor() : MaintenanceRepository {

    private var maintenanceDataMap = mutableMapOf<String, MaintenanceData>()

    override suspend fun getLoginMaintenance(): LfResult<MaintenanceData, GetMaintenanceError> =
        getMaintenance(LandingType.LOGIN)

    override suspend fun getDashboardMaintenance(): LfResult<MaintenanceData, GetMaintenanceError> =
        getMaintenance(LandingType.DASHBOARD)

    override suspend fun getRewardsMaintenance(): LfResult<MaintenanceData, GetMaintenanceError> =
        getMaintenance(LandingType.REWARDS)

    override suspend fun getShopMaintenance(): LfResult<MaintenanceData, GetMaintenanceError> =
        getMaintenance(LandingType.SHOP)

    override suspend fun getAccountMaintenance(): LfResult<MaintenanceData, GetMaintenanceError> =
        getMaintenance(LandingType.ACCOUNT)

    override suspend fun getDiscoverMaintenance(): LfResult<MaintenanceData, GetMaintenanceError> =
        getMaintenance(LandingType.DISCOVER)

    override suspend fun saveLoginMaintenance(maintenanceData: MaintenanceData) =
        saveMaintenance(LandingType.LOGIN, maintenanceData)

    override suspend fun saveDashboardMaintenance(maintenanceData: MaintenanceData) =
        saveMaintenance(LandingType.DASHBOARD, maintenanceData)

    override suspend fun saveRewardsMaintenance(maintenanceData: MaintenanceData) =
        saveMaintenance(LandingType.REWARDS, maintenanceData)

    override suspend fun saveShopMaintenance(maintenanceData: MaintenanceData) =
        saveMaintenance(LandingType.SHOP, maintenanceData)

    override suspend fun saveAccountMaintenance(maintenanceData: MaintenanceData) =
        saveMaintenance(LandingType.ACCOUNT, maintenanceData)

    override suspend fun saveDiscoverMaintenance(maintenanceData: MaintenanceData) =
        saveMaintenance(LandingType.DISCOVER, maintenanceData)

    private fun getMaintenance(landingType: LandingType): LfResult<MaintenanceData, GetMaintenanceError> {
        if (maintenanceDataMap.containsKey(landingType.path)) {
            maintenanceDataMap.get(landingType.path)?.let {
                return LfResult.success(it)
            } ?: return LfResult.failure(GetMaintenanceError.General(GeneralError.General))
        } else {
            return LfResult.failure(GetMaintenanceError.General(GeneralError.General))
        }
    }

    private fun saveMaintenance(
        landingType: LandingType,
        maintenanceData: MaintenanceData
    ) {
        maintenanceDataMap.put(landingType.path, maintenanceData)
    }
}
