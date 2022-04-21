/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.banners

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ph.com.globe.domain.banners.BannersDataManager
import ph.com.globe.errors.banners.FetchBannersError
import ph.com.globe.model.banners.BannerCarouselModel
import ph.com.globe.util.LfResult
import javax.inject.Inject

class NetworkBannersManager @Inject constructor(
    factory: BannersComponent.Factory
) : BannersDataManager {

    private val bannersComponent = factory.create()

    override suspend fun fetchBanners(): LfResult<List<BannerCarouselModel>, FetchBannersError> =
        withContext(Dispatchers.IO) {
            bannersComponent.provideFetchBannersNetworkCall().execute()
        }

    override suspend fun setBanners(banners: List<BannerCarouselModel>) {
        bannersComponent.provideBannersRepository().setBanners(banners)
    }

    override fun getBanners(): Flow<List<BannerCarouselModel>> =
        bannersComponent.provideBannersRepository().getBanners()
}
