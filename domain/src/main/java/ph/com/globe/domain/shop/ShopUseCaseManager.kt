/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.shop

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ph.com.globe.domain.shop.di.ShopComponent
import ph.com.globe.errors.shop.GetAllOffersError
import ph.com.globe.errors.shop.GetPromoSubscriptionHistoryError
import ph.com.globe.errors.shop.ValidateRetailerError
import ph.com.globe.model.shop.GetPromoSubscriptionHistoryParams
import ph.com.globe.model.shop.GetPromoSubscriptionHistoryResponse
import ph.com.globe.model.shop.domain_models.ShopItem
import ph.com.globe.util.LfResult
import javax.inject.Inject

class ShopUseCaseManager @Inject constructor(
    factory: ShopComponent.Factory
) : ShopDomainManager {

    private val shopComponent = factory.create()

    override suspend fun fetchOffers(forceRefresh: Boolean): LfResult<List<ShopItem>, GetAllOffersError> =
        shopComponent.provideFetchOffersUseCase().execute(forceRefresh)

    override fun getPromos(): Flow<List<ShopItem>> =
        shopComponent.provideGetPromosUseCase().execute()

    override fun getLoanable(): Flow<List<ShopItem>> =
        shopComponent.provideGetLoanableUseCase().execute()

    override fun getAllOffers(visibleOnMainCatalog: Boolean): Flow<List<ShopItem>> =
        shopComponent.provideGetAllOffersUseCase().execute(visibleOnMainCatalog)

    override fun getContentPromos(): Flow<List<ShopItem>> =
        shopComponent.provideGetContentPromosUseCase().execute()

    override suspend fun validateRetailer(serviceNumber: String): LfResult<Boolean, ValidateRetailerError> =
        withContext(Dispatchers.IO) {
            shopComponent.provideValidateRetailerUseCase().execute(serviceNumber)
        }

    override suspend fun getPromoSubscriptionHistory(params: GetPromoSubscriptionHistoryParams): LfResult<GetPromoSubscriptionHistoryResponse, GetPromoSubscriptionHistoryError> =
        withContext(Dispatchers.IO) {
            shopComponent.provideGetPromoSubscriptionHistoryUseCase().execute(params)
        }
}
