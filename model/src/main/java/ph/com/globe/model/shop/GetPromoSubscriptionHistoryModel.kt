/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.shop

import com.squareup.moshi.JsonClass
import ph.com.globe.model.account.toHeaderPair
import java.io.Serializable

data class GetPromoSubscriptionHistoryParams(
    val msisdn: String,
    val status: String = "All",
    val origin: String = "2",
    val silent: String = "1",
    val version: String = "2",
    val exclude: String = "0"
)

fun GetPromoSubscriptionHistoryParams.toQueryMap(): Map<String, String> =
    mapOf(
        msisdn.toHeaderPair(),
        "status" to status,
        "origin" to origin,
        "silent" to silent,
        "version" to version,
        "exclude" to exclude
    )

@JsonClass(generateAdapter = true)
data class GetPromoSubscriptionHistoryResponse(
    val result: PromoSubscriptionHistory
)

@JsonClass(generateAdapter = true)
data class PromoSubscriptionHistory(
    val mobileNumber: String,
    val brand: String,
    val subscriptions: List<PromoSubscriptionHistoryItem>
)

@JsonClass(generateAdapter = true)
data class PromoSubscriptionHistoryItem(
    val serviceId: String,
    val status: String,
    val creationDate: String,
    val expiryDate: String,
    val isINProvisioned: Boolean,
    val isINBPProvisioned: Boolean,
    val param: String,
    val smpService: String?,
    val promoName: String?,
    val includeDenomination: Boolean?,
    val optOutKeyword: String?,
    val accessCode: String?,
    val autoRenew: Boolean?
)

data class ContentSubscriptionUIModel(
    val serviceId: String = "",
    val promoName: String,
    val expiryDate: String,
    val description: String = "",
    val asset: String = "",
    val displayColor: String = "",
    var isActivated: Boolean = false,
    val redirectionLink: String = ""
) : Serializable
