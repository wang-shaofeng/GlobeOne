/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.banners

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BannersResponseModel(
    val data: BannersDataModel
)

@JsonClass(generateAdapter = true)
data class BannersDataModel(
    val field_mobile_section_component: List<BannerCarouselModel>
)

@JsonClass(generateAdapter = true)
data class BannerCarouselModel(
    val type: String,
    val field_display_rules: DisplayRulesModel?,
    val field_soft_banner_image: List<SoftBannerImageModel>?,
)

@JsonClass(generateAdapter = true)
data class DisplayRulesModel(
    val field_version_android: List<VersionModel>?
)

@JsonClass(generateAdapter = true)
data class VersionModel(
    val name: String?
)

@JsonClass(generateAdapter = true)
data class SoftBannerImageModel(
    val field_banner_title: String?,
    val field_subtext: String?,
    val field_cta: CtaObjectArray?,
    val field_image: ImageModel?
)

data class CtaObjectArray(val field_cta_list: List<CtaModel>)

@JsonClass(generateAdapter = true)
data class CtaModel(
    val field_cta_type: String,
    val field_link: LinkModel?
)

@JsonClass(generateAdapter = true)
data class LinkModel(
    val uri: String,
    val title: String?,
    val options: OptionsModel
)

@JsonClass(generateAdapter = true)
data class OptionsModel(
    val cta_type: String
)

@JsonClass(generateAdapter = true)
data class ImageModel(
    val field_media_image_2: MediaImageModel?
)

@JsonClass(generateAdapter = true)
data class MediaImageModel(
    val meta: MetaModel
)

@JsonClass(generateAdapter = true)
data class MetaModel(
    val url: String
)

data class Cta(
    val title: String?,
    val uri: String,
    val type: String
)

data class BannerModel(
    val title: String?,
    val subtext: String?,
    val primaryCTA: String? = null,
    val primaryCTALink: String? = null,
    val primaryCTAType: CTAType? = null,
    val secondaryCTA: String? = null,
    val secondaryCTALink: String? = null,
    val secondaryCTAType: CTAType? = null,
    val bannerURL: String?
)

enum class CTAType {
    DEEPLINK, EXTERNAL_LINK, OTHER
}

fun String.toCTAType() = when {
    this.equals(DEEPLINK, true) -> CTAType.DEEPLINK
    this.equals(EXTERNAL_LINK, true) -> CTAType.EXTERNAL_LINK
    else -> CTAType.OTHER
}

const val DEEPLINK = "DEEPLINK"
const val EXTERNAL_LINK = "EXTERNAL"
