/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.catalog

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ph.com.globe.domain.catalog.CatalogDataManager
import ph.com.globe.errors.catalog.*
import ph.com.globe.model.catalog.*
import ph.com.globe.util.LfResult
import javax.inject.Inject

class NetworkCatalogManager @Inject constructor(
    factory: CatalogComponent.Factory
) : CatalogDataManager {

    private val catalogComponent: CatalogComponent = factory.create()

    override suspend fun getContentSubscriptionStatus(params: ContentSubscriptionStatusParams): LfResult<ContentSubscriptionStatusResult, ContentSubscriptionStatusError> =
        withContext(Dispatchers.IO) {
            catalogComponent.provideContentSubscriptionStatusNetworkCall().execute(params)
        }

    override suspend fun provisionContentPromo(params: ProvisionContentPromoParams): LfResult<Unit, ProvisionContentPromoError> =
        withContext(Dispatchers.IO) {
            catalogComponent.provideProvisionContentPromoNetworkCall().execute(params)
        }

    override suspend fun unsubscribeContentPromo(params: UnsubscribeContentPromoParams): LfResult<Unit, UnsubscribeContentPromoError> =
        withContext(Dispatchers.IO) {
            catalogComponent.provideUnsubscribeContentPromoNetworkCall().execute(params)
        }
}
