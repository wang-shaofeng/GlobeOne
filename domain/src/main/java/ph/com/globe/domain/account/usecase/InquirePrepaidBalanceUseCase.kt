/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.account.usecase

import ph.com.globe.domain.account.AccountDataManager
import ph.com.globe.errors.account.InquirePrepaidBalanceError
import ph.com.globe.model.account.InquirePrepaidBalanceResult
import ph.com.globe.util.LfResult
import javax.inject.Inject

class InquirePrepaidBalanceUseCase @Inject constructor(private val accountManager: AccountDataManager) {

    suspend fun execute(msisdn: String): LfResult<InquirePrepaidBalanceResult, InquirePrepaidBalanceError> =
        accountManager.inquirePrepaidBalance(msisdn)

}
