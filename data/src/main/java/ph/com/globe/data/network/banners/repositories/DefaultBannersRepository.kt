/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.banners.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import ph.com.globe.model.banners.BannerCarouselModel
import javax.inject.Inject

class DefaultBannersRepository @Inject constructor() : BannersRepository {
    private val banners = MutableSharedFlow<List<BannerCarouselModel>>(1)

    override suspend fun setBanners(banners: List<BannerCarouselModel>) {
        this.banners.emit(banners)
    }

    override fun getBanners(): Flow<List<BannerCarouselModel>> = banners
}
