/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.rush.usecases

import ph.com.globe.domain.rush.RushDataManager
import ph.com.globe.domain.user_details.UserDetailsDataManager
import ph.com.globe.domain.utils.parseRushCampaignDate
import ph.com.globe.model.rush.RushRemoteConfigData
import ph.com.globe.util.fold
import javax.inject.Inject

class ShouldShowSpinwheelButtonUseCase @Inject constructor(
    private val rushDataManager: RushDataManager,
    private val userDetailsDataManager: UserDetailsDataManager
) {
    suspend fun execute(rushCampaignInfo: RushRemoteConfigData): Boolean {
        if (!rushCampaignInfo.isCampaignActive) {
            return false
        }

        val currentMillis = System.currentTimeMillis()
        val startDateMillis = rushCampaignInfo.startDate.parseRushCampaignDate()
        val endDateMillis = rushCampaignInfo.endDate.parseRushCampaignDate()

        if (startDateMillis != null && endDateMillis != null) {
            if (currentMillis !in startDateMillis..endDateMillis) {
                //don't show spinwheel if current date and time are not in range of start and end date
                return false
            }
        }

        CreateRushUserIfNeededAndGetUserAccessTokenUseCase(
            rushDataManager,
            userDetailsDataManager
        ).execute().fold({
            return true
        }, {
            return false
        })
    }
}
