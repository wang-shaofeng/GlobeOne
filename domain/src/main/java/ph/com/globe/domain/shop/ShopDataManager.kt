/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.shop

import ph.com.globe.errors.shop.GetAllOffersError
import ph.com.globe.errors.shop.GetPromoSubscriptionHistoryError
import ph.com.globe.errors.shop.ValidateRetailerError
import ph.com.globe.model.shop.GetPromoSubscriptionHistoryParams
import ph.com.globe.model.shop.GetPromoSubscriptionHistoryResponse
import ph.com.globe.model.shop.network_models.GetAllOffersResponse
import ph.com.globe.util.LfResult

interface ShopDataManager {

    suspend fun fetchData(): LfResult<GetAllOffersResponse, GetAllOffersError>

    suspend fun validateRetailer(serviceNumber: String): LfResult<Boolean, ValidateRetailerError>

    suspend fun getPromoSubscriptionHistory(params: GetPromoSubscriptionHistoryParams): LfResult<GetPromoSubscriptionHistoryResponse, GetPromoSubscriptionHistoryError>
}
