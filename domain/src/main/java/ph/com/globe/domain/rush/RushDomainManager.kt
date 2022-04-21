/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.rush

import ph.com.globe.model.rush.RushRemoteConfigData

interface RushDomainManager {

    suspend fun shouldShowSpinwheelButton(rushCampaignInfo: RushRemoteConfigData): Boolean

    suspend fun getSpinwheelUrl(rushCampaignInfo: RushRemoteConfigData): String?

    suspend fun getGameVouchersUrl(rushCampaignInfo: RushRemoteConfigData): String?
}
