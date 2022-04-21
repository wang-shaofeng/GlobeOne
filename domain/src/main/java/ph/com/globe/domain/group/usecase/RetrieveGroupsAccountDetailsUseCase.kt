/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.group.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.domain.ReposManager
import ph.com.globe.errors.group.AccountDetailsGroupsError
import ph.com.globe.model.group.domain_models.AccountDetailsGroups
import ph.com.globe.model.group.domain_models.AccountDetailsGroupsParams
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class RetrieveGroupsAccountDetailsUseCase @Inject constructor(
    reposManager: ReposManager
) : HasLogTag {

    private val accountGroupsRepo = reposManager.getAccountGroupsRepo()

    suspend fun execute(params: AccountDetailsGroupsParams): Flow<LfResult<AccountDetailsGroups?, AccountDetailsGroupsError>> =
        accountGroupsRepo.checkFreshnessAndUpdate(params).fold(
            {
                accountGroupsRepo.getAccountGroups(params.primaryMsisdn).map {
                    LfResult.success(it)
                }
            }, {
                flowOf(LfResult.failure(it))
            }
        )

    override val logTag = "RetrieveGroupsAccountDetailsUseCase"
}
