/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.shop

import kotlinx.coroutines.flow.Flow
import ph.com.globe.errors.shop.GetPromoSubscriptionHistoryError
import ph.com.globe.model.shop.GetPromoSubscriptionHistoryParams
import ph.com.globe.model.shop.GetPromoSubscriptionHistoryResponse
import ph.com.globe.errors.shop.GetAllOffersError
import ph.com.globe.errors.shop.ValidateRetailerError
import ph.com.globe.model.shop.domain_models.ShopItem
import ph.com.globe.util.LfResult

interface ShopDomainManager {

    suspend fun fetchOffers(forceRefresh: Boolean = false): LfResult<List<ShopItem>, GetAllOffersError>

    fun getPromos(): Flow<List<ShopItem>>

    fun getAllOffers(visibleOnMainCatalog: Boolean = true): Flow<List<ShopItem>>

    fun getLoanable(): Flow<List<ShopItem>>

    fun getContentPromos(): Flow<List<ShopItem>>

    suspend fun validateRetailer(serviceNumber: String): LfResult<Boolean, ValidateRetailerError>

    suspend fun getPromoSubscriptionHistory(params: GetPromoSubscriptionHistoryParams): LfResult<GetPromoSubscriptionHistoryResponse, GetPromoSubscriptionHistoryError>
}
