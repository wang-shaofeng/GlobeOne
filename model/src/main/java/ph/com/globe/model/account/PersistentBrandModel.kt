/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.account

import ph.com.globe.model.util.brand.AccountBrand

data class PersistentBrandModel(
    val accountName: String,
    val primaryMsisdn: String,
    val brand: AccountBrand
)

sealed class AccountsLoadingState {
    object Loading : AccountsLoadingState()
    object Loaded : AccountsLoadingState()
    object Failure : AccountsLoadingState()
}
