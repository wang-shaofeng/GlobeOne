/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.rewards.usecases

import ph.com.globe.domain.rewards.RewardsDataManager
import ph.com.globe.errors.rewards.GetRewardPointsError
import ph.com.globe.model.rewards.GetRewardPointsModel
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class GetRewardPointsUseCase @Inject constructor(
    private val rewardsDataManager: RewardsDataManager
) {
    suspend fun execute(msisdn: String, segment: String): LfResult<GetRewardPointsModel, GetRewardPointsError> {
        rewardsDataManager.getRewardPoints(msisdn, segment).fold({
            var sum = 0f
            it.result.loyaltyProgramDetails.forEach {
                it.totalPoints.toFloatOrNull()?.let { points -> sum += points }
            }
            val expiryDate: String = it.result.loyaltyProgramDetails.firstOrNull()
                ?.loyaltyPointsDetails?.firstOrNull()
                ?.wallets?.firstOrNull()?.expirationDate ?: ""
            val expiringAmount: String = it.result.loyaltyProgramDetails.firstOrNull()
                ?.loyaltyPointsDetails?.firstOrNull()
                ?.wallets?.firstOrNull()?.points ?: ""
            return LfResult.success(GetRewardPointsModel(sum, expiringAmount, expiryDate))
        }, {
            return LfResult.failure(it)
        })
    }
}
