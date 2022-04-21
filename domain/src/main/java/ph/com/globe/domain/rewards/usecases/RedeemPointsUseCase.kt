/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.rewards.usecases

import ph.com.globe.domain.rewards.RewardsDataManager
import ph.com.globe.errors.rewards.RedeemPointsError
import ph.com.globe.model.rewards.RedeemPointsResult
import ph.com.globe.util.LfResult
import javax.inject.Inject

class RedeemPointsUseCase @Inject constructor(
    private val rewardsDataManager: RewardsDataManager
) {
    suspend fun execute(
        msisdn: String,
        merchantNumber: String,
        amount: Float
    ): LfResult<RedeemPointsResult, RedeemPointsError> =
        rewardsDataManager.redeemPoints(msisdn, merchantNumber, amount)
}
