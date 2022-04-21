/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.group

import kotlinx.coroutines.flow.Flow
import ph.com.globe.errors.group.*
import ph.com.globe.model.group.*
import ph.com.globe.model.group.domain_models.AccountDetailsGroupsParams
import ph.com.globe.model.group.domain_models.AccountDetailsGroups
import ph.com.globe.util.LfResult

interface GroupDomainManager {

    suspend fun getGroupList(params: GetGroupListParams): LfResult<GetGroupListResponse, GetGroupListError>

    suspend fun addGroupMember(params: AddGroupMemberParams): LfResult<AddGroupMemberResponse, AddGroupMemberError>

    suspend fun retrieveGroupUsage(params: RetrieveGroupUsageParams): LfResult<RetrieveGroupUsageResponse, RetrieveGroupUsageError>

    suspend fun retrieveMemberUsage(params: RetrieveMemberUsageParams): LfResult<RetrieveMemberUsageResponse, RetrieveMemberUsageError>

    suspend fun deleteGroupMember(params: DeleteGroupMemberParams): LfResult<DeleteGroupMemberResponse, DeleteGroupMemberError>

    suspend fun setMemberUsageLimit(params: SetMemberUsageLimitParams): LfResult<Unit, SetMemberUsageLimitError>

    suspend fun retrieveGroupsAccountDetails(params: AccountDetailsGroupsParams): Flow<LfResult<AccountDetailsGroups?, AccountDetailsGroupsError>>
}
