/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */
package ph.com.globe.domain.rewards.usecases

import ph.com.globe.domain.rewards.RewardsDataManager
import ph.com.globe.errors.rewards.RedeemLoyaltyRewardsError
import ph.com.globe.model.rewards.LoyaltyProgramId
import ph.com.globe.model.rewards.RedeemLoyaltyRewardsResult
import ph.com.globe.model.rewards.RewardsCatalogItem
import ph.com.globe.util.LfResult
import javax.inject.Inject

class RedeemLoyaltyRewardsUseCase @Inject constructor(private val rewardsDataManager: RewardsDataManager) {
    suspend fun execute(
        mobileNumber: String,
        rewardsCatalogItem: RewardsCatalogItem,
        loyaltyProgramId: LoyaltyProgramId
    ): LfResult<RedeemLoyaltyRewardsResult, RedeemLoyaltyRewardsError> =
        rewardsDataManager.redeemLoyaltyRewards(mobileNumber, rewardsCatalogItem, loyaltyProgramId)
}
