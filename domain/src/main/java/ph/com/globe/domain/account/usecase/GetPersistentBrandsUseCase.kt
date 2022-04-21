/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.account.usecase

import kotlinx.coroutines.flow.Flow
import ph.com.globe.domain.account.AccountDataManager
import ph.com.globe.model.account.PersistentBrandModel
import javax.inject.Inject

class GetPersistentBrandsUseCase @Inject constructor(private val accountDataManager: AccountDataManager) {
    fun execute(): Flow<List<PersistentBrandModel>> = accountDataManager.getPersistentBrands()
}
