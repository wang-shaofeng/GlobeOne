/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.rush.usecases

import ph.com.globe.domain.rush.RushDataManager
import ph.com.globe.domain.user_details.UserDetailsDataManager
import ph.com.globe.model.rush.RushRemoteConfigData
import ph.com.globe.util.fold
import javax.inject.Inject

class GetGameVouchersUrlUseCase @Inject constructor(
    private val rushDataManager: RushDataManager,
    private val userDetailsDataManager: UserDetailsDataManager
) {

    suspend fun execute(rushCampaignInfo: RushRemoteConfigData): String? {
        CreateRushUserIfNeededAndGetUserAccessTokenUseCase(
            rushDataManager,
            userDetailsDataManager
        ).execute().fold({
            return rushCampaignInfo.micrositeURLVoucher.replace("[{user_token}]", it.access_token)
        }, {
            return null
        })
    }
}
