/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.group

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ph.com.globe.domain.group.GroupDataManager
import ph.com.globe.errors.group.*
import ph.com.globe.model.group.*
import ph.com.globe.util.LfResult
import javax.inject.Inject

class NetworkGroupManager @Inject constructor(
    factory: GroupComponent.Factory
) : GroupDataManager {

    private val groupComponent: GroupComponent = factory.create()

    override suspend fun getGroupList(params: GetGroupListParams): LfResult<GetGroupListResponse, GetGroupListError> =
        withContext(Dispatchers.IO) {
            groupComponent.provideGetGroupListNetworkCall().execute(params)
        }

    override suspend fun addGroupMember(params: AddGroupMemberParams): LfResult<AddGroupMemberResponse, AddGroupMemberError> =
        withContext(Dispatchers.IO) {
            groupComponent.provideAddGroupMemberNetworkCall().execute(params)
        }

    override suspend fun retrieveGroupUsage(params: RetrieveGroupUsageParams): LfResult<RetrieveGroupUsageResponse, RetrieveGroupUsageError> =
        withContext(Dispatchers.IO) {
            groupComponent.provideRetrieveGroupUsageNetworkCall().execute(params)
        }

    override suspend fun retrieveMemberUsage(params: RetrieveMemberUsageParams): LfResult<RetrieveMemberUsageResponse, RetrieveMemberUsageError> =
        withContext(Dispatchers.IO) {
            groupComponent.provideRetrieveMemberUsageNetworkCall().execute(params)
        }

    override suspend fun retrieveGroupService(params: RetrieveGroupServiceParams): LfResult<RetrieveGroupServiceResponse, RetrieveGroupServiceError> =
        withContext(Dispatchers.IO) {
            groupComponent.provideRetrieveGroupServiceNetworkCall().execute(params)
        }

    override suspend fun deleteGroupMember(params: DeleteGroupMemberParams): LfResult<DeleteGroupMemberResponse, DeleteGroupMemberError> =
        withContext(Dispatchers.IO) {
            groupComponent.provideDeleteGroupMemberNetworkCall().execute(params)
        }

    override suspend fun setMemberUsageLimit(params: SetMemberUsageLimitParams): LfResult<Unit, SetMemberUsageLimitError> =
        withContext(Dispatchers.IO) {
            groupComponent.provideSetMemberUsageLimitNetworkCall().execute(params)
        }
}
