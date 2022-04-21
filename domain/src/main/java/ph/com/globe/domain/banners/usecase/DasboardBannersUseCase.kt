/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.banners.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import ph.com.globe.domain.banners.BannersDataManager
import ph.com.globe.model.banners.BannerModel
import ph.com.globe.model.banners.Cta
import ph.com.globe.model.banners.toCTAType
import javax.inject.Inject

class DashboardBannersUseCase @Inject constructor(private val bannersManager: BannersDataManager) {

    fun get(): Flow<List<BannerModel>> = bannersManager.getBanners().mapLatest { banners ->
        val list: MutableList<BannerModel> = mutableListOf()
        banners.forEach { bannersCarousel ->
            with(bannersCarousel) {
                if (type == SOFT_BANNER_CAROUSEL) {
                    field_soft_banner_image?.forEach { banner ->
                        var primaryCTA: Cta? = null
                        var secondaryCTA: Cta? = null
                        with(banner) {
                            field_cta?.field_cta_list?.forEach { cta ->
                                with(cta) {
                                    if (field_cta_type == CTA_PRIMARY) {
                                        field_link?.let {
                                            primaryCTA = Cta(
                                                it.title,
                                                it.uri,
                                                it.options.cta_type
                                            )
                                        }
                                    } else {
                                        field_link?.let {
                                            secondaryCTA = Cta(
                                                it.title,
                                                it.uri,
                                                it.options.cta_type
                                            )
                                        }
                                    }
                                }
                            }
                            list.add(
                                BannerModel(
                                    title = field_banner_title,
                                    subtext = field_subtext,
                                    primaryCTA = primaryCTA?.title,
                                    primaryCTALink = primaryCTA?.uri,
                                    primaryCTAType = primaryCTA?.type?.toCTAType(),
                                    secondaryCTA = secondaryCTA?.title,
                                    secondaryCTALink = secondaryCTA?.uri,
                                    secondaryCTAType = secondaryCTA?.type?.toCTAType(),
                                    bannerURL = field_image?.field_media_image_2?.meta?.url
                                )
                            )
                        }
                    }
                }
            }
        }
        return@mapLatest list
    }
}

private const val SOFT_BANNER_CAROUSEL = "globe_app_component--soft_banner_carousel"
private const val CTA_PRIMARY = "primary"
