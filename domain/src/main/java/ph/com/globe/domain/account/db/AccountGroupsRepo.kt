/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.account.db

import kotlinx.coroutines.flow.Flow
import ph.com.globe.errors.group.AccountDetailsGroupsError
import ph.com.globe.model.group.domain_models.AccountDetailsGroupsParams
import ph.com.globe.model.group.domain_models.AccountDetailsGroups
import ph.com.globe.util.LfResult

interface AccountGroupsRepo {

    suspend fun fetchAccountGroups(params: AccountDetailsGroupsParams): LfResult<Unit, AccountDetailsGroupsError>

    suspend fun checkFreshnessAndUpdate(params: AccountDetailsGroupsParams): LfResult<Unit, AccountDetailsGroupsError>

    suspend fun getAccountGroups(primaryMsisdn: String): Flow<AccountDetailsGroups?>

    suspend fun refreshAccountGroups(primaryMsisdn: String)

    suspend fun invalidateAccountGroups(primaryMsisdn: String)

    suspend fun deleteAllAccountsGroups()

    suspend fun deleteMetadata()
}
