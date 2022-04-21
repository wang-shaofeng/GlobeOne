/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.balance

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ph.com.globe.domain.balance.BalanceDataManager
import ph.com.globe.errors.balance.CheckBalanceSufficiencyError
import ph.com.globe.model.balance.CheckAmaxWalletBalanceSufficiencyParams
import ph.com.globe.model.balance.CheckBalanceSufficiencyParams
import ph.com.globe.util.LfResult
import javax.inject.Inject

class NetworkBalanceManager @Inject constructor(
    factory: BalanceComponent.Factory
) : BalanceDataManager {

    private val balanceComponent = factory.create()

    override suspend fun checkBalanceSufficiency(params: CheckBalanceSufficiencyParams): LfResult<Boolean, CheckBalanceSufficiencyError> =
        withContext(Dispatchers.IO) {
            balanceComponent.provideCheckBalanceSufficiencyNetworkCall().execute(params)
        }

    override suspend fun checkAmaxWalletBalanceSufficiency(params: CheckAmaxWalletBalanceSufficiencyParams): LfResult<Boolean, CheckBalanceSufficiencyError> =
        withContext(Dispatchers.IO) {
            balanceComponent.provideCheckAmaxWalletBalanceSufficiencyNetworkCall().execute(params)
        }

}
