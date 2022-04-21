/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.maintenance.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.maintenance.MaintenanceRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.maintenance.GetMaintenanceError
import ph.com.globe.globeonesuperapp.data.BuildConfig
import ph.com.globe.model.maintenance.MaintenanceData
import ph.com.globe.model.maintenance.MaintenanceResponse
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class GetMaintenanceNetworkCall @Inject constructor(
    private val maintenanceRetrofit: MaintenanceRetrofit
) : HasLogTag {

    private suspend fun execute(landingType: LandingType): LfResult<MaintenanceData, GetMaintenanceError> {
        val response = kotlin.runCatching {
            maintenanceRetrofit.getMaintenance(
                landingType.path,
                mapOf(
                    "version" to BuildConfig.VERSION_NAME.extractVersionNameNumber(),
                    "mobile_type" to "android"
                )
            )
        }.fold(Response<MaintenanceResponse>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold({
            logSuccessfulNetworkCall()
            LfResult.success(it.data)
        }, {
            logFailedNetworkCall(it)
            LfResult.failure(it.toSpecific())
        })
    }

    suspend fun getLoginMaintenance(): LfResult<MaintenanceData, GetMaintenanceError> =
        execute(LandingType.LOGIN)

    suspend fun getDashboardMaintenance(): LfResult<MaintenanceData, GetMaintenanceError> =
        execute(LandingType.DASHBOARD)

    suspend fun getRewardsMaintenance(): LfResult<MaintenanceData, GetMaintenanceError> =
        execute(LandingType.REWARDS)

    suspend fun getShopMaintenance(): LfResult<MaintenanceData, GetMaintenanceError> =
        execute(LandingType.SHOP)

    suspend fun getAccountMaintenance(): LfResult<MaintenanceData, GetMaintenanceError> =
        execute(LandingType.ACCOUNT)

    suspend fun getDiscoverMaintenance(): LfResult<MaintenanceData, GetMaintenanceError> =
        execute(LandingType.DISCOVER)

    override val logTag: String = "GetMaintenanceNetworkCall"
}

enum class LandingType(val path: String) {

    LOGIN("login_landing"),

    DASHBOARD("dashboard_landing"),

    REWARDS("rewards_landing"),

    SHOP("shop_landing"),

    ACCOUNT("account_landing"),

    DISCOVER("discover_landing"),
}

private fun NetworkError.toSpecific() = GetMaintenanceError.General(GeneralError.Other(this))
