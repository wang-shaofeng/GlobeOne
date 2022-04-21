/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.rewards.usecases

import ph.com.globe.domain.rewards.RewardsDataManager
import ph.com.globe.errors.rewards.AddDataConversionError
import ph.com.globe.model.rewards.AddDataConversionRequest
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class AddDataConversionUseCase @Inject constructor(private val rewardsManager: RewardsDataManager) {

    suspend fun execute(requestBody: AddDataConversionRequest): LfResult<String, AddDataConversionError> =
        rewardsManager.addDataConversion(requestBody).fold({
            LfResult.success(it)
        },{
            LfResult.failure(it)
        })
}
