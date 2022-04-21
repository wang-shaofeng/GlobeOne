/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.rewards.usecases

import kotlinx.coroutines.flow.Flow
import ph.com.globe.domain.rewards.RewardsDataManager
import ph.com.globe.model.rewards.RewardsCatalogItem
import javax.inject.Inject

class GetAllRewardsUseCase @Inject constructor(private val dataManager: RewardsDataManager) {
    fun get(): Flow<List<RewardsCatalogItem>> = dataManager.getRewards()
}
