/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.banners

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ph.com.globe.domain.banners.di.BannersComponent
import ph.com.globe.errors.banners.FetchBannersError
import ph.com.globe.model.banners.BannerCarouselModel
import ph.com.globe.model.banners.BannerModel
import ph.com.globe.util.LfResult
import javax.inject.Inject

class BannersUseCaseManager @Inject constructor(
    factory: BannersComponent.Factory
) : BannersDomainManager {

    private val bannersComponent = factory.create()

    override suspend fun fetchBanners(): LfResult<List<BannerCarouselModel>, FetchBannersError> =
        withContext(Dispatchers.IO) {
            bannersComponent.provideFetchBannersUseCase().execute()
        }

    override fun getDashboardBanners(): Flow<List<BannerModel>> =
        bannersComponent.provideDashboardBannersUseCase().get()
}
