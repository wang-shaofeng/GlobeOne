/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.payment

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetGCashAccountInfoResponse(
    val result: GetGCashAccountResult
)

@JsonClass(generateAdapter = true)
data class GetGCashAccountResult(
    val nickname: String,
    val status: String,
    val userId: String
)
