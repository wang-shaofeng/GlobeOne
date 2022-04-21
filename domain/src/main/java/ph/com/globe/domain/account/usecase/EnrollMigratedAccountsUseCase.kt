/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.account.usecase

import ph.com.globe.domain.account.AccountDataManager
import ph.com.globe.errors.account.EnrollMigratedAccountsError
import ph.com.globe.model.account.EnrollMigratedAccountsParams
import ph.com.globe.model.account.EnrollMigratedAccountsResponse
import ph.com.globe.util.LfResult
import javax.inject.Inject

class EnrollMigratedAccountsUseCase @Inject constructor(private val accountManager: AccountDataManager) {

    suspend fun execute(params: EnrollMigratedAccountsParams): LfResult<EnrollMigratedAccountsResponse, EnrollMigratedAccountsError> =
        accountManager.enrollMigratedAccounts(params)
}
