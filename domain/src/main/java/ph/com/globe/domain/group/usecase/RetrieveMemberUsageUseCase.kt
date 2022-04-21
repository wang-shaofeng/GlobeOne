/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.group.usecase

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.domain.group.GroupDataManager
import ph.com.globe.errors.group.RetrieveMemberUsageError
import ph.com.globe.model.group.RetrieveMemberUsageParams
import ph.com.globe.model.group.RetrieveMemberUsageResponse
import ph.com.globe.util.LfResult
import javax.inject.Inject

class RetrieveMemberUsageUseCase @Inject constructor(
    private val groupDataManager: GroupDataManager
) : HasLogTag {

    suspend fun execute(params: RetrieveMemberUsageParams): LfResult<RetrieveMemberUsageResponse, RetrieveMemberUsageError> =
        groupDataManager.retrieveMemberUsage(params)

    override val logTag = "RetrieveMemberUsageUseCase"
}
