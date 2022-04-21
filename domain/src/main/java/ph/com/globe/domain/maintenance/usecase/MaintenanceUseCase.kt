/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.maintenance.usecase

import ph.com.globe.domain.maintenance.MaintenanceDataManager
import ph.com.globe.errors.maintenance.GetMaintenanceError
import ph.com.globe.model.banners.toCTAType
import ph.com.globe.model.maintenance.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class MaintenanceUseCase @Inject constructor(private val maintenanceDataManager: MaintenanceDataManager) {

    suspend fun getLoginMaintenance(): LfResult<MaintenanceModel, GetMaintenanceError> =
        maintenanceDataManager.getLoginMaintenance()
            .fold({
                LfResult.success(MaintenanceModel(
                    outerMaintenance = it.getOuterMaintenance(MaintenanceTabId.LOGIN_OUTER),
                    componentsMap = it.components?.filterNot { it.pageTabId.isNullOrEmpty() }
                        ?.associate { component ->
                            component.pageTabId!! to MaintenanceUIModel(
                                pageTabId = component.getPageTabId(),
                                hasMaintenance = component.hasMaintenance(),
                                imageUrl = component.getImageUrl(),
                                title = component.getTitle(),
                                content = component.getContent(),
                                cta = component.getCta()
                            )
                        }
                ))
            }, {
                LfResult.failure(it)
            })

    suspend fun getDashboardMaintenance(): LfResult<MaintenanceModel, GetMaintenanceError> =
        maintenanceDataManager.getDashboardMaintenance()
            .fold({
                LfResult.success(MaintenanceModel(
                    outerMaintenance = it.getOuterMaintenance(MaintenanceTabId.DASHBOARD_OUTER),
                    componentsMap = it.components?.filterNot { it.pageTabId.isNullOrEmpty() }
                        ?.associate { component ->
                            component.pageTabId!! to MaintenanceUIModel(
                                pageTabId = component.getPageTabId(),
                                hasMaintenance = component.hasMaintenance(),
                                imageUrl = component.getImageUrl(),
                                title = component.getTitle(),
                                content = component.getContent(),
                                cta = component.getCta(),
                            )
                        }
                ))
            }, {
                LfResult.failure(it)
            })

    suspend fun getRewardsMaintenance(): LfResult<MaintenanceModel, GetMaintenanceError> =
        maintenanceDataManager.getRewardsMaintenance()
            .fold({
                LfResult.success(MaintenanceModel(
                    outerMaintenance = it.getOuterMaintenance(MaintenanceTabId.REWARDS_OUTER),
                    componentsMap = it.components?.filterNot { it.pageTabId.isNullOrEmpty() }
                        ?.associate { component ->
                            component.pageTabId!! to MaintenanceUIModel(
                                pageTabId = component.getPageTabId(),
                                hasMaintenance = component.hasMaintenance(),
                                imageUrl = component.getImageUrl(),
                                title = component.getTitle(),
                                content = component.getContent(),
                                cta = component.getCta()
                            )
                        }
                ))
            }, {
                LfResult.failure(it)
            })

    suspend fun getShopMaintenance(): LfResult<MaintenanceModel, GetMaintenanceError> =
        maintenanceDataManager.getShopMaintenance()
            .fold({
                LfResult.success(MaintenanceModel(
                    outerMaintenance = it.getOuterMaintenance(MaintenanceTabId.SHOP_OUTER),
                    componentsMap = it.components?.filterNot { it.pageTabId.isNullOrEmpty() }
                        ?.associate { component ->
                            component.pageTabId!! to MaintenanceUIModel(
                                pageTabId = component.getPageTabId(),
                                hasMaintenance = component.hasMaintenance(),
                                imageUrl = component.getImageUrl(),
                                title = component.getTitle(),
                                content = component.getContent(),
                                cta = component.getCta()
                            )
                        }
                ))
            }, {
                LfResult.failure(it)
            })

    suspend fun getAccountMaintenance(): LfResult<MaintenanceModel, GetMaintenanceError> =
        maintenanceDataManager.getAccountMaintenance()
            .fold({
                LfResult.success(MaintenanceModel(
                    outerMaintenance = it.getOuterMaintenance(MaintenanceTabId.ACCOUNT_OUTER),
                    componentsMap = it.components?.filterNot { it.pageTabId.isNullOrEmpty() }
                        ?.associate { component ->
                            component.pageTabId!! to MaintenanceUIModel(
                                pageTabId = component.getPageTabId(),
                                hasMaintenance = component.hasMaintenance(),
                                imageUrl = component.getImageUrl(),
                                title = component.getTitle(),
                                content = component.getContent(),
                                cta = component.getCta()
                            )
                        }
                ))
            }, {
                LfResult.failure(it)
            })

    suspend fun getDiscoverMaintenance(): LfResult<MaintenanceModel, GetMaintenanceError> =
        maintenanceDataManager.getDiscoverMaintenance()
            .fold({
                LfResult.success(MaintenanceModel(
                    outerMaintenance = it.getOuterMaintenance(MaintenanceTabId.DISCOVER_OUTER),
                    componentsMap = it.components?.filterNot { it.pageTabId.isNullOrEmpty() }
                        ?.associate { component ->
                            component.pageTabId!! to MaintenanceUIModel(
                                pageTabId = component.getPageTabId(),
                                hasMaintenance = component.hasMaintenance(),
                                imageUrl = component.getImageUrl(),
                                title = component.getTitle(),
                                content = component.getContent(),
                                cta = component.getCta()
                            )
                        }
                ))
            }, {
                LfResult.failure(it)
            })

    private fun MaintenanceData.hasOuterMaintenance() =
        MAINTENANCE_TYPE.equals(maintenancePage?.type, true)

    private fun MaintenanceComponent.getPageTabId() =
        pageTabId ?: ""

    private fun MaintenanceComponent.hasMaintenance() =
        PUBLISHED.equals(state, true) && maintenancePage != null

    private fun MaintenanceComponent.getCta() =
        maintenancePage?.cta?.firstOrNull()?.run {
            CtaUIModel(
                title = link?.title,
                ctaLink = link?.uri,
                ctaType = link?.options?.ctaType?.toCTAType()
            )
        }

    private fun MaintenanceComponent.getImageUrl() = maintenancePage?.image?.mediaImage?.meta?.url
        ?: ""

    private fun MaintenanceComponent.getTitle() = maintenancePage?.title ?: ""

    private fun MaintenanceComponent.getContent() = maintenancePage?.subtext ?: ""

    private fun MaintenanceData.getOuterMaintenance(pageTabId: String) =
        MaintenanceUIModel(
            pageTabId = pageTabId,
            hasMaintenance = hasOuterMaintenance(),
            imageUrl = maintenancePage?.image?.mediaImage?.meta?.url
                ?: "",
            title = maintenancePage?.title ?: "",
            content = maintenancePage?.subtext ?: "",
            cta = maintenancePage?.cta?.firstOrNull()?.run {
                CtaUIModel(
                    title = link?.title,
                    ctaLink = link?.uri,
                    ctaType = link?.options?.ctaType?.toCTAType()
                )
            }
        )
}

private const val MAINTENANCE_TYPE = "globe_app_component--maintenance"
private const val PUBLISHED = "published"
