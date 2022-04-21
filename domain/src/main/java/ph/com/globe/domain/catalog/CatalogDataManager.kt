/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.catalog

import ph.com.globe.errors.catalog.ContentSubscriptionStatusError
import ph.com.globe.errors.catalog.ProvisionContentPromoError
import ph.com.globe.errors.catalog.UnsubscribeContentPromoError
import ph.com.globe.model.catalog.*
import ph.com.globe.util.LfResult

interface CatalogDataManager {

    suspend fun getContentSubscriptionStatus(params: ContentSubscriptionStatusParams): LfResult<ContentSubscriptionStatusResult, ContentSubscriptionStatusError>

    suspend fun provisionContentPromo(params: ProvisionContentPromoParams): LfResult<Unit, ProvisionContentPromoError>

    suspend fun unsubscribeContentPromo(params: UnsubscribeContentPromoParams): LfResult<Unit, UnsubscribeContentPromoError>

}
