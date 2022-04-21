/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.account.usecase

import ph.com.globe.domain.account.AccountDataManager
import ph.com.globe.errors.profile.GetEnrolledAccountsError
import ph.com.globe.model.account.GetMigratedAccountsParams
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.util.LfResult
import javax.inject.Inject

class GetMigratedAccountsUseCase @Inject constructor(private val accountManager: AccountDataManager) {

    suspend fun execute(params: GetMigratedAccountsParams): LfResult<List<EnrolledAccount>, GetEnrolledAccountsError> =
        accountManager.getMigratedAccounts(params)

}
