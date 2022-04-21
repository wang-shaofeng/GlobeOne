/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.account

import com.squareup.moshi.JsonClass
import ph.com.globe.model.util.brand.SEGMENT_KEY
import java.io.Serializable

data class GetCustomerCampaignParams(
    val msisdn: String,
    val channel: String,
    val segment: String,
    val mode: String = "",
    val customAttributes: String = ""
)

fun GetCustomerCampaignParams.toQueryMap() = mapOf(
    msisdn.toHeaderPair(),
    "channel" to channel,
    SEGMENT_KEY to segment
)

fun GetCustomerCampaignParams.toPersonalizedCampaignQueryMap(): Map<String, String> {
    val queryMap: MutableMap<String, String> = mutableMapOf(
        msisdn.toHeaderPair(),
        "channel" to channel,
        "segment" to segment,
        "mode" to mode
    )

    // This segment adds "customAttributes" to queryMap as payload if mode is either "2001" or "2002".
    if (mode == "2001" || mode == "2002") {
        queryMap["customAttributes"] = customAttributes
    }

    return queryMap
}

@JsonClass(generateAdapter = true)
data class GetCustomerCampaignPromoResponse(val result: GetCustomerCampaignPromoResult)

@JsonClass(generateAdapter = true)
data class GetCustomerCampaignPromoResult(
    val brand: String?,
    val mobileNumber: String?,
    val availablePromos: List<AvailablePromosResult>
)

@JsonClass(generateAdapter = true)
data class AvailablePromosResult(
    val promoType: String,
    val promoName: String,
    val description: String,
    val promoMechanics: String,
    val validityDate: String,
    val skuId1: String?,
    val skuParameter1: String,
    val skuId2: String,
    val skuParameter2: String,
    val skuId3: String,
    val skuParameter3: String,
    val maId: String,
    val customerParameter1: String,
    val customerParameter2: String,
    val benefitsSkuDays: String?
)

data class AvailableCampaignPromosModel(
    val maId: String,
    val promoMechanics: String,
    val customerParameter1: String,
    val channel: String,
    val mobileNumber: String,
    val skuId1: String?,
    val description: String,
    val benefitsSkuDays: String? = null,
    var promoType: PersonalizedCampaignsPromoType? = null,
    var bannerUrl: String? = null,
    var buttonLabel: String? = null,
    var availMode: Int = 0
) : Serializable {

    enum class PersonalizedCampaignsPromoType { EXCLUSIVE_OFFERS, FREEBIE_OR_SURPRISE, SIM_SAMPLER, NONE }
}

fun AvailablePromosResult.toModel(channel: String, mobileNumber: String) =
    AvailableCampaignPromosModel(
        maId,
        promoMechanics,
        customerParameter1,
        channel,
        mobileNumber,
        skuId1,
        description,
        benefitsSkuDays
    )
