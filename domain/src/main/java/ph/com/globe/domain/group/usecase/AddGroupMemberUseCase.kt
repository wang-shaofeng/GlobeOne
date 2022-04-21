/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.group.usecase

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.domain.group.GroupDataManager
import ph.com.globe.errors.group.AddGroupMemberError
import ph.com.globe.model.group.AddGroupMemberResponse
import ph.com.globe.model.group.AddGroupMemberParams
import ph.com.globe.util.LfResult
import javax.inject.Inject

class AddGroupMemberUseCase @Inject constructor(
    private val groupDataManager: GroupDataManager
) : HasLogTag {

    suspend fun execute(params: AddGroupMemberParams): LfResult<AddGroupMemberResponse, AddGroupMemberError> =
        groupDataManager.addGroupMember(params)

    override val logTag = "AddGroupMemberUseCase"
}
