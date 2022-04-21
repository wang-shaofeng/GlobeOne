/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.account.usecase

import ph.com.globe.domain.account.AccountDataManager
import ph.com.globe.model.account.AccountsLoadingState
import javax.inject.Inject

class SetAccountsLoadingStateUseCase @Inject constructor(private val accountDataManager: AccountDataManager) {

    suspend fun execute(state: AccountsLoadingState) =
        accountDataManager.setAccountsLoadingState(state)
}
