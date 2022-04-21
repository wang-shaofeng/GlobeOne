/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.rewards.usecases

import ph.com.globe.domain.rewards.RewardsDataManager
import ph.com.globe.model.rewards.LoyaltyProgramId
import javax.inject.Inject

class GetFreeRandomRewardsUseCase @Inject constructor(private val dataManager: RewardsDataManager) {
    fun get(num: Int, loyaltyProgramId: LoyaltyProgramId) =
        dataManager.getFreeRandomRewards(num, loyaltyProgramId)
}
