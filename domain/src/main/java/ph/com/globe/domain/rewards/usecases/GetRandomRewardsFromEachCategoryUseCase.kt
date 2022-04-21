/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.rewards.usecases

import ph.com.globe.domain.rewards.RewardsDataManager
import javax.inject.Inject

class GetRandomRewardsFromEachCategoryUseCase @Inject constructor(private val dataManager: RewardsDataManager) {
    fun get() = dataManager.getRandomFromEachCategory()
}
