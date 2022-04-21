/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.rush

import ph.com.globe.domain.rush.di.RushComponent
import ph.com.globe.model.rush.RushRemoteConfigData
import javax.inject.Inject

class RushUseCaseManager @Inject constructor(
    factory: RushComponent.Factory
) : RushDomainManager {

    private val rushComponent: RushComponent = factory.create()

    override suspend fun shouldShowSpinwheelButton(rushCampaignInfo: RushRemoteConfigData): Boolean =
        rushComponent.shouldShowSpinwheelButton().execute(rushCampaignInfo)

    override suspend fun getSpinwheelUrl(rushCampaignInfo: RushRemoteConfigData): String? =
        rushComponent.getSpinwheelUrl().execute(rushCampaignInfo)

    override suspend fun getGameVouchersUrl(rushCampaignInfo: RushRemoteConfigData): String? =
        rushComponent.getGameVouchersUrl().execute(rushCampaignInfo)
}
