/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.account.usecase

import ph.com.globe.domain.account.AccountDataManager
import ph.com.globe.errors.account.GetAccountStatusError
import ph.com.globe.model.account.GetAccountStatusParams
import ph.com.globe.model.account.GetAccountStatusResult
import ph.com.globe.util.LfResult
import javax.inject.Inject

class GetAccountStatusUseCase @Inject constructor(
    private val accountManager: AccountDataManager
) {

    suspend fun execute(params: GetAccountStatusParams): LfResult<GetAccountStatusResult, GetAccountStatusError> =
        accountManager.getAccountStatus(params)
}
