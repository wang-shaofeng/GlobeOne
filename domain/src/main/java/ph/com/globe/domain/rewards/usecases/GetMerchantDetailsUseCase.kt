/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.rewards.usecases

import ph.com.globe.domain.rewards.RewardsDataManager
import ph.com.globe.errors.rewards.GetMerchantDetailsError
import ph.com.globe.model.rewards.GetMerchantDetailsResult
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class GetMerchantDetailsUseCase @Inject constructor(
    private val rewardsDataManager: RewardsDataManager
) {
    suspend fun executeUsingUUID(uuid: String): LfResult<GetMerchantDetailsResult, GetMerchantDetailsError> =
        rewardsDataManager.getMerchantDetailsUsingUUID(uuid).fold({
            val convertedNumber = it.mobileNumber.convertTo63()
            LfResult.success(it.copy(mobileNumber = convertedNumber))
        }, {
            LfResult.failure(it)
        })

    suspend fun executeUsingMobileNumber(mobileNumber: String): LfResult<GetMerchantDetailsResult, GetMerchantDetailsError> =
        rewardsDataManager.getMerchantDetailsUsingMobileNumber(mobileNumber).fold({
            // convert leading 0 to 63, because redeem rewards points api requests number with 639*****
            val convertedNumber = it.mobileNumber.convertTo63()
            LfResult.success(it.copy(mobileNumber = convertedNumber))
        }, {
            LfResult.failure(it)
        })

    // convert leading 0 to 63, because redeem rewards points api requests number with 639*****
    private fun String.convertTo63() = if (this.startsWith("0")) {
        "63" + this.removePrefix("0")
    } else {
        this
    }
}
