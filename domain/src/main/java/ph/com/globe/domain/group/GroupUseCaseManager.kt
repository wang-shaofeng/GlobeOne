/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.group

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ph.com.globe.domain.group.di.GroupComponent
import ph.com.globe.errors.group.*
import ph.com.globe.model.group.*
import ph.com.globe.model.group.domain_models.AccountDetailsGroupsParams
import ph.com.globe.model.group.domain_models.AccountDetailsGroups
import ph.com.globe.util.LfResult
import javax.inject.Inject

class GroupUseCaseManager @Inject constructor(
    factory: GroupComponent.Factory
) : GroupDomainManager {

    private val groupComponent: GroupComponent = factory.create()

    override suspend fun getGroupList(params: GetGroupListParams): LfResult<GetGroupListResponse, GetGroupListError> =
        withContext(Dispatchers.IO) {
            groupComponent.provideGetGroupListUseCase().execute(params)
        }

    override suspend fun addGroupMember(params: AddGroupMemberParams): LfResult<AddGroupMemberResponse, AddGroupMemberError> =
        withContext(Dispatchers.IO) {
            groupComponent.provideAddGroupMemberUseCase().execute(params)
        }

    override suspend fun retrieveGroupUsage(params: RetrieveGroupUsageParams): LfResult<RetrieveGroupUsageResponse, RetrieveGroupUsageError> =
        withContext(Dispatchers.IO) {
            groupComponent.provideRetrieveGroupUsageUseCase().execute(params)
        }

    override suspend fun retrieveMemberUsage(params: RetrieveMemberUsageParams): LfResult<RetrieveMemberUsageResponse, RetrieveMemberUsageError> =
        withContext(Dispatchers.IO) {
            groupComponent.provideRetrieveMemberUsageUseCase().execute(params)
        }

    override suspend fun deleteGroupMember(params: DeleteGroupMemberParams): LfResult<DeleteGroupMemberResponse, DeleteGroupMemberError> =
        withContext(Dispatchers.IO) {
            groupComponent.provideDeleteGroupMemberUseCase().execute(params)
        }

    override suspend fun setMemberUsageLimit(params: SetMemberUsageLimitParams): LfResult<Unit, SetMemberUsageLimitError> =
        withContext(Dispatchers.IO) {
            groupComponent.provideSetMemberUsageLimitUseCase().execute(params)
        }

    override suspend fun retrieveGroupsAccountDetails(params: AccountDetailsGroupsParams): Flow<LfResult<AccountDetailsGroups?, AccountDetailsGroupsError>> =
        withContext(Dispatchers.IO) {
            groupComponent.provideRetrieveGroupsAccountDetailsUseCase().execute(params)
        }
}
