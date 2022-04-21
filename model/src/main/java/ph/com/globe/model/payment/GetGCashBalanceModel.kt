/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.payment

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetGCashBalanceResponse(
    val result: List<GetGCashBalanceResult>
)

@JsonClass(generateAdapter = true)
data class GetGCashBalanceResult(
    val availableAmount: GCashAmount
)

@JsonClass(generateAdapter = true)
data class GCashAmount(
    val amount: Float
)
