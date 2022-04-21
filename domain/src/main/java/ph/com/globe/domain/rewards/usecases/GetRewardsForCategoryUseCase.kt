/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.rewards.usecases

import kotlinx.coroutines.flow.mapLatest
import ph.com.globe.domain.rewards.RewardsDataManager
import ph.com.globe.model.rewards.LoyaltyProgramId
import ph.com.globe.model.rewards.RewardsCategory
import javax.inject.Inject

class GetRewardsForCategoryUseCase @Inject constructor(private val dataManager: RewardsDataManager) {
    fun get(points: Float, loyaltyProgramId: LoyaltyProgramId, category: RewardsCategory) =
        dataManager.getRewards().mapLatest { list ->
            list.filter {
                (it.category == category || category == RewardsCategory.NONE) && (it.pointsCost.toInt() <= points || points == -1f) && (loyaltyProgramId in it.loyaltyProgramIds || loyaltyProgramId == LoyaltyProgramId.ALL)
            }
        }
}
