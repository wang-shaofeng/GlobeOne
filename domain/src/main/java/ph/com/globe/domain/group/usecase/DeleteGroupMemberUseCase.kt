/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.group.usecase

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.domain.group.GroupDataManager
import ph.com.globe.errors.group.DeleteGroupMemberError
import ph.com.globe.model.group.DeleteGroupMemberParams
import ph.com.globe.model.group.DeleteGroupMemberResponse
import ph.com.globe.util.LfResult
import javax.inject.Inject

class DeleteGroupMemberUseCase @Inject constructor(
    private val groupDataManager: GroupDataManager
) : HasLogTag {

    suspend fun execute(params: DeleteGroupMemberParams): LfResult<DeleteGroupMemberResponse, DeleteGroupMemberError> =
        groupDataManager.deleteGroupMember(params)

    override val logTag = "DeleteGroupMemberUseCase"
}
