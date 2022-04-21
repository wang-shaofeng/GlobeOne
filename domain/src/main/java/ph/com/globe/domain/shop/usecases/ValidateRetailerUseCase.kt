/*
  * Copyright (C) 2021 LotusFlare
  * All Rights Reserved.
  * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
  */

package ph.com.globe.domain.shop.usecases

import ph.com.globe.domain.shop.ShopDataManager
import ph.com.globe.errors.shop.ValidateRetailerError
import ph.com.globe.util.LfResult
import javax.inject.Inject

class ValidateRetailerUseCase @Inject constructor(
    private val shopManager: ShopDataManager
) {
    suspend fun execute(serviceNumber: String): LfResult<Boolean, ValidateRetailerError> =
        shopManager.validateRetailer(serviceNumber)
}
