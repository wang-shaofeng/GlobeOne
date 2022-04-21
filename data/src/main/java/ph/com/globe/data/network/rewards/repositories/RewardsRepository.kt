/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.rewards.repositories

import kotlinx.coroutines.flow.Flow
import ph.com.globe.model.rewards.LoyaltyProgramId
import ph.com.globe.model.rewards.RewardsCatalogItem

interface RewardsRepository {
    suspend fun setRewardsCatalog(list: List<RewardsCatalogItem>)

    fun getRandomFromEachCategory(): Flow<List<RewardsCatalogItem>>

    fun getRandomFromEachCategoryDependsOnPoints(
        points: Float,
        loyaltyProgramId: LoyaltyProgramId
    ): Flow<List<RewardsCatalogItem>>

    fun getFreeRandomRewards(
        num: Int,
        loyaltyProgramId: LoyaltyProgramId
    ): Flow<List<RewardsCatalogItem>>

    fun getRewards(): Flow<List<RewardsCatalogItem>>
}
