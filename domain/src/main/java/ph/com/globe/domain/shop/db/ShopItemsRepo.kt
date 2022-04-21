/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.shop.db

import kotlinx.coroutines.flow.Flow
import ph.com.globe.errors.shop.GetAllOffersError
import ph.com.globe.model.shop.domain_models.ShopItem
import ph.com.globe.util.LfResult

interface ShopItemsRepo {

    suspend fun fetchShopItems(): LfResult<Unit, GetAllOffersError>

    suspend fun checkFreshnessAndUpdate(forceRefresh: Boolean = false): LfResult<Unit, GetAllOffersError>

    fun getAllOffers(visibleOnMainCatalog: Boolean): Flow<List<ShopItem>>

    fun getPromos(): Flow<List<ShopItem>>

    fun getLoanable(): Flow<List<ShopItem>>

    fun getContentPromos(): Flow<List<ShopItem>>

    suspend fun refreshShopItems()

    suspend fun invalidateShopItems()

    suspend fun deleteShopItems()

    suspend fun deleteMetadata()
}
