/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.rewards.usecases

import ph.com.globe.domain.rewards.RewardsDataManager
import ph.com.globe.errors.rewards.LoyaltyCustomerProfileError
import ph.com.globe.model.rewards.LoyaltyProgramId
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class GetLoyaltyProgramIdFromSpecificMsisdnUseCase @Inject constructor(private val dataManager: RewardsDataManager) {
    suspend fun execute(mobileNumber: String): LfResult<LoyaltyProgramId, LoyaltyCustomerProfileError> =
        dataManager.getLoyaltyCustomerProfile(mobileNumber).fold(
            {
                LfResult.success(it.loyaltyProgramId)
            },
            {
                LfResult.failure(it)
            })
}
