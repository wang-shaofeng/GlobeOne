/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.shop.usecases

import kotlinx.coroutines.flow.first
import ph.com.globe.domain.ReposManager
import ph.com.globe.errors.shop.GetAllOffersError
import ph.com.globe.model.shop.domain_models.ShopItem
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class FetchOffersUseCase @Inject constructor(reposManager: ReposManager) {

    private val shopItemsRepo = reposManager.getShopItemsRepo()

    suspend fun execute(forceRefresh: Boolean): LfResult<List<ShopItem>, GetAllOffersError> =
        shopItemsRepo.checkFreshnessAndUpdate(forceRefresh).fold(
            {
                LfResult.success(shopItemsRepo.getAllOffers(false).first())
            }, {
                LfResult.failure(it)
            }
        )
}
