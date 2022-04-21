/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.catalog.usecase

import ph.com.globe.domain.catalog.CatalogDataManager
import ph.com.globe.errors.catalog.ContentSubscriptionStatusError
import ph.com.globe.model.catalog.ContentSubscriptionStatusParams
import ph.com.globe.model.catalog.ContentSubscriptionStatusResult
import ph.com.globe.util.LfResult
import javax.inject.Inject

class ContentSubscriptionStatusUseCase @Inject constructor(private val catalogManager: CatalogDataManager) {

    suspend fun execute(params: ContentSubscriptionStatusParams): LfResult<ContentSubscriptionStatusResult, ContentSubscriptionStatusError> =
        catalogManager.getContentSubscriptionStatus(params)
}
