/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.account.repositories

import kotlinx.coroutines.flow.Flow
import ph.com.globe.model.account.AccountsLoadingState
import ph.com.globe.model.account.PersistentBrandModel

interface AccountRepository {

    fun getPersistentBrands(): Flow<List<PersistentBrandModel>>

    fun getAccountsLoadingState(): Flow<AccountsLoadingState>

    suspend fun storeBrands(brands: List<PersistentBrandModel>)

    suspend fun setAccountsLoadingState(state: AccountsLoadingState)
}
