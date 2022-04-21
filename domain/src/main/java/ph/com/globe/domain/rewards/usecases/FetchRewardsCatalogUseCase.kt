/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.rewards.usecases

import ph.com.globe.domain.rewards.RewardsDataManager
import javax.inject.Inject

class FetchRewardsCatalogUseCase @Inject constructor(private val rewardsManager: RewardsDataManager) {
    suspend fun execute() =
        rewardsManager.fetchRewardsCatalog().also {
            it.value?.let { rewardsManager.setRewardsCatalog(it) }
        }
}
