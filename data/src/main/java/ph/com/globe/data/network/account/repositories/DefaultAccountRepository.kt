/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.account.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import ph.com.globe.model.account.AccountsLoadingState
import ph.com.globe.model.account.PersistentBrandModel
import javax.inject.Inject

class DefaultAccountRepository @Inject constructor() : AccountRepository {

    private val persistentBrandFlow = MutableSharedFlow<List<PersistentBrandModel>>(1)

    private val accountsLoadingStateFlow = MutableSharedFlow<AccountsLoadingState>(1)

    override fun getPersistentBrands(): Flow<List<PersistentBrandModel>> = persistentBrandFlow

    override fun getAccountsLoadingState(): Flow<AccountsLoadingState> = accountsLoadingStateFlow

    override suspend fun storeBrands(brands: List<PersistentBrandModel>) {
        persistentBrandFlow.emit(brands)
    }

    override suspend fun setAccountsLoadingState(state: AccountsLoadingState) {
        accountsLoadingStateFlow.emit(state)
    }
}
