/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.shop.usecases

import kotlinx.coroutines.flow.Flow
import ph.com.globe.domain.ReposManager
import ph.com.globe.model.shop.domain_models.ShopItem
import javax.inject.Inject

class GetLoanableUseCase @Inject constructor(reposManager: ReposManager) {

    private val shopItemsRepo = reposManager.getShopItemsRepo()

    fun execute(): Flow<List<ShopItem>> = shopItemsRepo.getLoanable()
}
