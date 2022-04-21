/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.db.shop

import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ph.com.globe.data.DataScope
import ph.com.globe.data.db.DataWithFreshnessAndValidity
import ph.com.globe.data.db.QueryTimeFreshnessDao
import ph.com.globe.data.db.di.ONE_DAY_FRESHNESS
import ph.com.globe.data.db.shop.ShopItemEntity.Companion.TABLE_NAME
import javax.inject.Inject
import javax.inject.Named

@Dao
abstract class GlobeShopItemsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    internal abstract suspend fun insertShopItems(items: List<ShopItemEntity>)

    @Query("DELETE FROM shop_items")
    internal abstract suspend fun deleteShopItems()

    @Synchronized
    @Transaction
    open suspend fun clearInsert(items: List<ShopItemEntity>) {
        deleteShopItems()
        insertShopItems(items)
    }

    @Query("SELECT * FROM shop_items")
    internal abstract fun getAllShopItems(): Flow<List<ShopItemEntity>?>

    @Query("SELECT * FROM shop_items WHERE visibleOnMainCatalog = 1")
    internal abstract fun getAllVisibleShopItems(): Flow<List<ShopItemEntity>?>

    @Query("SELECT * FROM shop_items WHERE visibleOnMainCatalog = 1 AND isContent = 0 AND loanable = 0 AND isFreebie = 0")
    internal abstract fun getPromos(): Flow<List<ShopItemEntity>?>

    @Query("SELECT * FROM shop_items WHERE visibleOnMainCatalog = 1 AND loanable = 1")
    internal abstract fun getLoanable(): Flow<List<ShopItemEntity>?>

    @Query("SELECT * FROM shop_items WHERE visibleOnMainCatalog = 1 AND isContent = 1")
    internal abstract fun getContentPromos(): Flow<List<ShopItemEntity>?>
}

@DataScope
class GlobeShopItemsQueryDao @Inject constructor(
    private val shopItemsDao: GlobeShopItemsDao,
    @Named(ONE_DAY_FRESHNESS) private val queryTimeFreshness: QueryTimeFreshnessDao
) {

    @Synchronized
    suspend fun clearInsert(items: List<ShopItemEntity>) {
        withContext(Dispatchers.IO) {
            shopItemsDao.clearInsert(items)
            queryTimeFreshness.markFreshRow(TABLE_NAME)
        }
    }

    fun getFreshness(): Flow<DataWithFreshnessAndValidity<List<ShopItemEntity>>> {
        val freshnessFlow = queryTimeFreshness.getValidityAndFreshness(TABLE_NAME)

        return freshnessFlow.combine(shopItemsDao.getAllShopItems()) { validityAndFreshness, shopItems ->
            DataWithFreshnessAndValidity.dataWithFreshnessAndValidity(
                shopItems,
                validityAndFreshness
            )
        }.flowOn(Dispatchers.IO)
    }

    fun getAllShopItems(): Flow<List<ShopItemEntity>?> = shopItemsDao.getAllShopItems()

    fun getAllVisibleShopItems(): Flow<List<ShopItemEntity>?> =
        shopItemsDao.getAllVisibleShopItems()

    fun getPromos(): Flow<List<ShopItemEntity>?> = shopItemsDao.getPromos()

    fun getLoanable(): Flow<List<ShopItemEntity>?> = shopItemsDao.getLoanable()

    fun getContentPromos(): Flow<List<ShopItemEntity>?> = shopItemsDao.getContentPromos()

    @Synchronized
    suspend fun staleRow() = queryTimeFreshness.markStaleRow(TABLE_NAME)

    @Synchronized
    suspend fun invalidRow() = queryTimeFreshness.markInvalidRow(TABLE_NAME)

    @Synchronized
    suspend fun deleteShopItems() = shopItemsDao.deleteShopItems()

    suspend fun deleteMetadata() = queryTimeFreshness.deleteData(TABLE_NAME)
}
