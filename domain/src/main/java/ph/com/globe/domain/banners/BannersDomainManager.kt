/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.banners

import kotlinx.coroutines.flow.Flow
import ph.com.globe.errors.banners.FetchBannersError
import ph.com.globe.model.banners.BannerCarouselModel
import ph.com.globe.model.banners.BannerModel
import ph.com.globe.util.LfResult

interface BannersDomainManager {

    suspend fun fetchBanners(): LfResult<List<BannerCarouselModel>, FetchBannersError>

    fun getDashboardBanners(): Flow<List<BannerModel>>
}
