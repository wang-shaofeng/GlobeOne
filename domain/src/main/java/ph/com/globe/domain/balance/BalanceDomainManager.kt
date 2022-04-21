/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.balance

import ph.com.globe.errors.balance.CheckBalanceSufficiencyError
import ph.com.globe.model.balance.CheckAmaxWalletBalanceSufficiencyParams
import ph.com.globe.model.balance.CheckBalanceSufficiencyParams
import ph.com.globe.util.LfResult

interface BalanceDomainManager {

    suspend fun checkBalanceSufficiency(params: CheckBalanceSufficiencyParams): LfResult<Boolean, CheckBalanceSufficiencyError>

    suspend fun checkAmaxWalletBalanceSufficiency(params: CheckAmaxWalletBalanceSufficiencyParams): LfResult<Boolean, CheckBalanceSufficiencyError>

}
