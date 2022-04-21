/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.banners.usecase

import ph.com.globe.domain.banners.BannersDataManager
import javax.inject.Inject

class FetchBannersUseCase @Inject constructor(private val bannersManager: BannersDataManager) {
    suspend fun execute() =
        bannersManager.fetchBanners().also {
            it.value?.let { bannersManager.setBanners(it) }
        }
}
