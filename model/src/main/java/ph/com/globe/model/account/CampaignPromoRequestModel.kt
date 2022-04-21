/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.account

import com.squareup.moshi.JsonClass
import ph.com.globe.model.util.brand.MOBILE_SEGMENT

@JsonClass(generateAdapter = true)
data class CampaignPromoRequestModel(
    val mobileNumber: String,
    val channel: String,
    val tag: String,
    val keyword: String,
    val segment: String = MOBILE_SEGMENT
)

@JsonClass(generateAdapter = true)
data class CampaignPromoResponseModel(
    val result: CampaignPromoResult
)

@JsonClass(generateAdapter = true)
data class CampaignPromoResult(
    val transactionId: String
)
