/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.db.shop

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.DataScope
import ph.com.globe.data.db.needsUpdate
import ph.com.globe.data.db.util.ParameterlessRepoUpdater
import ph.com.globe.domain.shop.ShopDataManager
import ph.com.globe.domain.shop.db.ShopItemsRepo
import ph.com.globe.errors.shop.GetAllOffersError
import ph.com.globe.model.shop.domain_models.ShopItem
import ph.com.globe.model.shop.network_models.GetAllOffersResponse
import ph.com.globe.util.LfResult
import javax.inject.Inject

/**
 * Repository for [ShopItem]s. Exposes [fetchShopItems] functions, which will try to fetch
 * enrolled accounts from [shopItemsQueryDao] first, and if there are no stored shop items or
 * the data that was stored is stale, it will start the update via [parameterlessRepoUpdater]'s
 * update method.
 * Also, it exposes functions [getAllOffers] which get all offers, [getPromos] which get promos,
 * [getLoanable] which get loans and [getContentPromos] which get contents.
 */
@DataScope
class ShopItemsRepository @Inject constructor(
    private val shopItemsQueryDao: GlobeShopItemsQueryDao,
    private val shopDataManager: ShopDataManager,
    private val parameterlessRepoUpdater: ParameterlessRepoUpdater<GetAllOffersResponse, GetAllOffersError>
) : ShopItemsRepo, HasLogTag {

    override suspend fun fetchShopItems(): LfResult<Unit, GetAllOffersError> {
        parameterlessRepoUpdater.update(shopDataManager::fetchData) { getAllOffersResponse ->
            val items = mutableListOf<ShopItemEntity>()

            getAllOffersResponse.offer_by_id.forEach {
                items.add(it.value.toEntity(getAllOffersResponse))
            }

            shopItemsQueryDao.clearInsert(items)
        }?.let {
            return LfResult.failure(it)
        }

        return LfResult.success(Unit)
    }

    override suspend fun checkFreshnessAndUpdate(forceRefresh: Boolean): LfResult<Unit, GetAllOffersError> {
        val shopItemsWithFreshness = shopItemsQueryDao.getFreshness().first()
        if (shopItemsWithFreshness.needsUpdate() || forceRefresh) {
            return fetchShopItems()
        }

        return LfResult.success(Unit)
    }

    override fun getAllOffers(visibleOnMainCatalog: Boolean): Flow<List<ShopItem>> =
        if (visibleOnMainCatalog) shopItemsQueryDao.getAllVisibleShopItems().toDomain()
        else shopItemsQueryDao.getAllShopItems().toDomain()

    override fun getPromos(): Flow<List<ShopItem>> = shopItemsQueryDao.getPromos().toDomain()

    override fun getLoanable(): Flow<List<ShopItem>> = shopItemsQueryDao.getLoanable().toDomain()

    override fun getContentPromos(): Flow<List<ShopItem>> =
        shopItemsQueryDao.getContentPromos().toDomain()

    override suspend fun refreshShopItems() {
        shopItemsQueryDao.staleRow()
    }

    override suspend fun invalidateShopItems() {
        shopItemsQueryDao.invalidRow()
    }

    override suspend fun deleteShopItems() {
        shopItemsQueryDao.deleteShopItems()
    }

    override suspend fun deleteMetadata() {
        shopItemsQueryDao.deleteMetadata()
    }

    override val logTag = "ShopItemsItemsRepo"
}
