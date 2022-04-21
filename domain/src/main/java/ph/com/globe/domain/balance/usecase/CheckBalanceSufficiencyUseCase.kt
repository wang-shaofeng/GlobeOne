/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.balance.usecase

import ph.com.globe.domain.balance.BalanceDataManager
import ph.com.globe.errors.balance.CheckBalanceSufficiencyError
import ph.com.globe.model.balance.CheckBalanceSufficiencyParams
import ph.com.globe.util.LfResult
import javax.inject.Inject

class CheckBalanceSufficiencyUseCase @Inject constructor(private val balanceManager: BalanceDataManager) {

    suspend fun execute(params: CheckBalanceSufficiencyParams): LfResult<Boolean, CheckBalanceSufficiencyError> =
        balanceManager.checkBalanceSufficiency(params)

}
