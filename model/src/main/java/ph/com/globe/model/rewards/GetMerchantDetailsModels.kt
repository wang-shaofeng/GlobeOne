/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.rewards

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetMerchantDetailsResponse(
    val result: GetMerchantDetailsResult
)

@JsonClass(generateAdapter = true)
data class GetMerchantDetailsResult(
    val uuid: String,
    val branchName: String,
    val merchantId: Int,
    val merchantName: String,
    val minimumPoints: String,
    val mobileNumber: String // merchant number
)
