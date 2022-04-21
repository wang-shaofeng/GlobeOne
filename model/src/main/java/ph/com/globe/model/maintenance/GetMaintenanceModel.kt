/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.maintenance

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MaintenanceResponse(
    val data: MaintenanceData
)

@JsonClass(generateAdapter = true)
data class MaintenanceData(
    @Json(name = "field_maintenance_page")
    val maintenancePage: MaintenancePage?,

    @Json(name = "field_mobile_section_component")
    val components: List<MaintenanceComponent>?
)

@JsonClass(generateAdapter = true)
data class MaintenanceComponent(
    val type: String?,

    @Json(name = "moderation_state")
    val state: String?,

    @Json(name = "field_page_tab_id")
    val pageTabId: String?,

    @Json(name = "field_maintenance_page")
    val maintenancePage: MaintenancePage?
)

@JsonClass(generateAdapter = true)
data class MaintenancePage(
    val type: String?,

    @Json(name = "field_banner_title")
    val title: String?,

    @Json(name = "field_subtext")
    val subtext: String?,

    @Json(name = "field_cta")
    val cta: List<CtaModel>?,

    @Json(name = "field_image")
    val image: ImageModel?
)

@JsonClass(generateAdapter = true)
data class CtaModel(
    @Json(name = "field_cta_type")
    val ctaType: String?,

    @Json(name = "field_link")
    val link: LinkModel?
)

@JsonClass(generateAdapter = true)
data class LinkModel(
    val uri: String?,
    val title: String?,
    val options: OptionsModel?
)

@JsonClass(generateAdapter = true)
data class OptionsModel(
    @Json(name = "cta_type")
    val ctaType: String?
)

@JsonClass(generateAdapter = true)
data class ImageModel(
    @Json(name = "field_media_image_2")
    val mediaImage: MediaImageModel?
)

@JsonClass(generateAdapter = true)
data class MediaImageModel(
    val meta: MetaModel?
)

@JsonClass(generateAdapter = true)
data class MetaModel(
    val url: String?
)
