/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.shop.network_models

import com.squareup.moshi.JsonClass
import ph.com.globe.model.util.FREEBIE
import ph.com.globe.model.util.MAIN

@JsonClass(generateAdapter = true)
data class GetAllOffersParams(
    val operator_name: String = "globe",
    val language_code: String = "en"
)

@JsonClass(generateAdapter = true)
data class GetAllOffersResponse(
    val category_by_id: Map<String, CategoryJson>,
    val offer_by_id: Map<String, OfferJson>,
    val product_by_id: Map<String, ProductJson>,
    val service_by_id: Map<String, ServiceJson>
)

@JsonClass(generateAdapter = true)
data class CategoryJson(
    val id: String?,
    val data: CategoryDataJson
)

@JsonClass(generateAdapter = true)
data class CategoryDataJson(
    val name: String?,
    val description: String?,
    val sort_priority: Int?,
    val parent: String?,
    val misc_params: CategoryDataMiscParamsJson?
)

@JsonClass(generateAdapter = true)
data class CategoryDataMiscParamsJson(
    val booster: String?
)

@JsonClass(generateAdapter = true)
data class OfferJson(
    val id: String?,
    val amount_breakdown: OfferAmountJson,
    val data: OfferDataJson
)

@JsonClass(generateAdapter = true)
data class OfferAmountJson(
    val final: String?,
    val initial: String?,
    val discount: String?
)

@JsonClass(generateAdapter = true)
data class OfferDataJson(
    val name: String?,
    val description: String?,
    val sort_priority: Int?,
    val start_time: Int?,
    val end_time: Int?,
    val amounts: OfferDataAmountJson,
    val categories: List<String>?,
    val products: List<String>,
    val misc_params: OfferDataMiscParamsJson?,
    val static_assets: OfferDataStaticAssetJson?
)

@JsonClass(generateAdapter = true)
data class OfferDataAmountJson(
    val primary: String?
)

@JsonClass(generateAdapter = true)
data class OfferDataMiscParamsJson(
    val nf_serviceid_withcharging: String?,
    val nf_serviceid_wocharging: String?,
    val nf_param_withcharging: String?,
    val nf_param_wocharging: String?,
    val brand_mainaccounttype_tm: String?,
    val brand_mainaccounttype_tmpostpaid: String?,
    val brand_mainaccounttype_prepaid: String?,
    val brand_mainaccounttype_hpw: String?,
    val brand_mainaccounttype_gpmyfi: String?,
    val brand_mainaccounttype_tmmyfi: String?,
    val brand_segment_ltp: String?,
    val brand_segment_mobile: String?,
    val brand_segment_bb: String?,
    val brand_customertype_sg: String?,
    val brand_customertype_customer: String?,
    val type: String?,
    val discount: String?,
    val load_allocation: String?,
    val method: String?,
    val salsap_keyword: String?,
    val salsap_servicefee: String?,
    val loan_type: String?,
    val loan_servicefee: String?,
    val loan_creditscore: String?,
    val payment_gcash_promo: String?,
    val payment_allowgcashpayment: String?,
    val payment_allowccdcpayment: String?,
    val api_apisubscribe: String?,
    val api_apistop: String?,
    val keyword_omskeyword: String?,
    val keyword_keywordsubscribe: String?,
    val keyword_keywordstop: String?,
    val partner_partnerid: String?,
    val partner_platform: String?,
    val partner_partnername: String?,
    val partner_redirectionlink: String?,
    val partner_willappend: String?,
    val skellig_keyword: String?,
    val skellig_wallet: String?,
    val skellig_category: String?,
    val asset: String?,
    val duration: Long?,
    val denom_category: String?
)

@JsonClass(generateAdapter = true)
data class OfferDataStaticAssetJson(
    val display_visibleonappmaincatalog: String?,
    val display_featured: String?,
    val display_channel: String?,
    val display_color: String?,
    val display_tags: String?,
    val display_monitoredinapp: String?
)

@JsonClass(generateAdapter = true)
data class ProductJson(
    val id: String?,
    val data: ProductDataJson
)

@JsonClass(generateAdapter = true)
data class ProductDataJson(
    val name: String?,
    val description: String?,
    val duration: Long?,
    val services: List<String>?,
    val misc_params: ProductDataMiscParamsJson?
)

@JsonClass(generateAdapter = true)
data class ProductDataMiscParamsJson(
    val type: String?,
    val param: String?,
    val sequence: String?
)

@JsonClass(generateAdapter = true)
data class ServiceJson(
    val id: String?,
    val data: ServiceDataJson
)

@JsonClass(generateAdapter = true)
data class ServiceDataJson(
    val name: String?,
    val description: String?,
    val service_type: Int?, // 1 - Data, 2 - Voice, 3 - SMS
    val rate_period: Long?,
    val unit_amount: Long?,
    val charge_type: String?,
    val misc_params: ServiceDataMiscParamsJson?
)

@JsonClass(generateAdapter = true)
data class ServiceDataMiscParamsJson(
    val type: String?,
    val booster_appname_appicon: String?,
    val freebie_appname_appicon: String?,
    val appdata_appname_appicon: String?,
    val duration: String?
)

fun ServiceJson.isFreebie() =
    this.data.misc_params?.type == FREEBIE

fun ServiceJson.getUnitAmount() =
    this.data.unit_amount ?: 0L

fun ServiceJson.hasUnlimitedAmount() =
    this.data.charge_type == UNLIMITED_STRING_INDICATOR

fun ServiceJson.hasDuration() =
    this.data.misc_params?.duration != null && getDuration() != 0L

fun ServiceJson.getDuration(): Long =
    this.data.misc_params?.duration?.toLongOrNull() ?: 0L

fun ProductJson?.hasDuration() =
    this?.data?.duration != null && getDuration() != 0L

fun ProductJson?.getDuration(): Long =
    this?.data?.duration ?: 0L

fun ProductJson?.isMainProduct(): Boolean =
    this?.data?.misc_params?.type == MAIN

// the catalog response returns the "unlimited" string for unlimited offers
private const val UNLIMITED_STRING_INDICATOR = "unlimited"
