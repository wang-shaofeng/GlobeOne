/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.shop

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ph.com.globe.domain.shop.ShopDataManager
import ph.com.globe.errors.shop.GetAllOffersError
import ph.com.globe.errors.shop.GetPromoSubscriptionHistoryError
import ph.com.globe.errors.shop.ValidateRetailerError
import ph.com.globe.model.shop.GetPromoSubscriptionHistoryParams
import ph.com.globe.model.shop.GetPromoSubscriptionHistoryResponse
import ph.com.globe.model.shop.network_models.GetAllOffersResponse
import ph.com.globe.util.LfResult
import javax.inject.Inject

class NetworkShopManager @Inject constructor(
    factory: ShopComponent.Factory
) : ShopDataManager {

    private val shopComponent = factory.create()

    override suspend fun fetchData(): LfResult<GetAllOffersResponse, GetAllOffersError> =
        withContext(Dispatchers.IO) {
            shopComponent.provideGetAllOffersNetworkCall().execute()
        }

    override suspend fun validateRetailer(serviceNumber: String): LfResult<Boolean, ValidateRetailerError> =
        shopComponent.provideValidateRetailerNetworkCall().execute(serviceNumber)

    override suspend fun getPromoSubscriptionHistory(params: GetPromoSubscriptionHistoryParams): LfResult<GetPromoSubscriptionHistoryResponse, GetPromoSubscriptionHistoryError> =
        withContext(Dispatchers.IO) {
            shopComponent.provideGetPromoSubscriptionHistoryNetworkCall().execute(params)
        }
}
