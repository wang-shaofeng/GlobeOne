/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.catalog

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ph.com.globe.domain.catalog.di.CatalogComponent
import ph.com.globe.errors.catalog.*
import ph.com.globe.model.catalog.*
import ph.com.globe.util.LfResult
import javax.inject.Inject

class CatalogUseCaseManager @Inject constructor(
    factory: CatalogComponent.Factory
) : CatalogDomainManager {

    private val catalogComponent: CatalogComponent = factory.create()

    override suspend fun getContentSubscriptionStatus(params: ContentSubscriptionStatusParams): LfResult<ContentSubscriptionStatusResult, ContentSubscriptionStatusError> =
        withContext(Dispatchers.IO) {
            catalogComponent.provideContentSubscriptionStatusUseCase().execute(params)
        }

    override suspend fun provisionContentPromo(params: ProvisionContentPromoParams): LfResult<Unit, ProvisionContentPromoError> =
        withContext(Dispatchers.IO) {
            catalogComponent.provideProvisionContentPromoUseCase().execute(params)
        }

    override suspend fun unsubscribeContentPromo(params: UnsubscribeContentPromoParams): LfResult<Unit, UnsubscribeContentPromoError> =
        withContext(Dispatchers.IO) {
            catalogComponent.provideUnsubscribeContentPromoUseCase().execute(params)
        }
}
