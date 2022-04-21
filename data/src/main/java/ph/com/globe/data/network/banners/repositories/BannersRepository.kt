/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.banners.repositories

import kotlinx.coroutines.flow.Flow
import ph.com.globe.model.banners.BannerCarouselModel

interface BannersRepository {

    suspend fun setBanners(banners: List<BannerCarouselModel>)

    fun getBanners(): Flow<List<BannerCarouselModel>>
}
