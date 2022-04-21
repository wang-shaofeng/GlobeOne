/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.catalog.usecase

import ph.com.globe.domain.catalog.CatalogDataManager
import ph.com.globe.errors.catalog.UnsubscribeContentPromoError
import ph.com.globe.model.catalog.UnsubscribeContentPromoParams
import ph.com.globe.util.LfResult
import javax.inject.Inject

class UnsubscribeContentPromoUseCase @Inject constructor(private val catalogManager: CatalogDataManager) {

    suspend fun execute(params: UnsubscribeContentPromoParams): LfResult<Unit, UnsubscribeContentPromoError> =
        catalogManager.unsubscribeContentPromo(params)
}
