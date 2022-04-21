/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.shop.usecases

import ph.com.globe.domain.shop.ShopDataManager
import ph.com.globe.errors.shop.GetPromoSubscriptionHistoryError
import ph.com.globe.model.shop.GetPromoSubscriptionHistoryParams
import ph.com.globe.model.shop.GetPromoSubscriptionHistoryResponse
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class GetPromoSubscriptionHistoryUseCase @Inject constructor(
    private val shopDataManager: ShopDataManager
) {

    suspend fun execute(params: GetPromoSubscriptionHistoryParams): LfResult<GetPromoSubscriptionHistoryResponse, GetPromoSubscriptionHistoryError> {
        return shopDataManager.getPromoSubscriptionHistory(params)
    }
}
