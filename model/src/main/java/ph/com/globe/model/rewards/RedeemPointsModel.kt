/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.rewards

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RedeemPointsRequestModel(
    val mobileNumber: String?,
    val accountNumber: String?,
    val merchantNumber: String,
    val amount: Float
)

@JsonClass(generateAdapter = true)
data class RedeemPointsResponseModel(
    val result: RedeemPointsResult
)

@JsonClass(generateAdapter = true)
data class RedeemPointsResult(
    val transactionNumber: String
)
