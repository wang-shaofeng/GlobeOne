/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.group.usecase

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.domain.group.GroupDataManager
import ph.com.globe.errors.group.RetrieveGroupUsageError
import ph.com.globe.model.group.RetrieveGroupUsageParams
import ph.com.globe.model.group.RetrieveGroupUsageResponse
import ph.com.globe.util.LfResult
import javax.inject.Inject

class RetrieveGroupUsageUseCase @Inject constructor(
    private val groupDataManager: GroupDataManager
) : HasLogTag {

    suspend fun execute(params: RetrieveGroupUsageParams): LfResult<RetrieveGroupUsageResponse, RetrieveGroupUsageError> =
        groupDataManager.retrieveGroupUsage(params)

    override val logTag = "RetrieveGroupUsageUseCase"
}
